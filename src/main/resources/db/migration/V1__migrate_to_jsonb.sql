-- Міграція для перетворення структури таблиць на JSONB
-- Ця міграція додає JSONB колонки та мігрує дані з окремих колонок в JSONB
-- Старі колонки залишаються для сумісності з існуючим кодом

-- ============================================
-- TRAVEL PLANS: Додавання JSONB колонки та міграція даних
-- ============================================

-- Додаємо JSONB колонку для зберігання всіх даних travel plan
ALTER TABLE travel_plans 
ADD COLUMN IF NOT EXISTS data JSONB DEFAULT '{}'::jsonb;

-- Мігруємо існуючі дані з окремих колонок в JSONB
-- Використовуємо jsonb_build_object для створення JSON об'єкта
UPDATE travel_plans
SET data = jsonb_build_object(
    'title', title,
    'description', description,
    'start_date', start_date::text,
    'end_date', end_date::text,
    'budget', budget::text,
    'currency', COALESCE(currency, 'USD'),
    'is_public', COALESCE(is_public, false)
)
WHERE data = '{}'::jsonb OR data IS NULL;

-- Додаємо GIN індекс для швидкого пошуку по JSONB полю
CREATE INDEX IF NOT EXISTS idx_travel_plans_data ON travel_plans USING GIN (data);

-- Додаємо коментар
COMMENT ON COLUMN travel_plans.data IS 'JSONB field containing all travel plan data (title, description, dates, budget, currency, is_public)';

-- ============================================
-- LOCATIONS: Додавання JSONB колонки та міграція даних
-- ============================================

-- Додаємо JSONB колонку для зберігання всіх даних location
ALTER TABLE locations 
ADD COLUMN IF NOT EXISTS data JSONB DEFAULT '{}'::jsonb;

-- Мігруємо існуючі дані з окремих колонок в JSONB
UPDATE locations
SET data = jsonb_build_object(
    'name', name,
    'address', address,
    'latitude', latitude::text,
    'longitude', longitude::text,
    'visit_order', visit_order,
    'arrival_date', arrival_date::text,
    'departure_date', departure_date::text,
    'budget', budget::text,
    'notes', notes
)
WHERE data = '{}'::jsonb OR data IS NULL;

-- Додаємо GIN індекс для швидкого пошуку по JSONB полю
CREATE INDEX IF NOT EXISTS idx_locations_data ON locations USING GIN (data);

-- Додаємо коментар
COMMENT ON COLUMN locations.data IS 'JSONB field containing all location data (name, address, coordinates, dates, budget, notes)';

-- ============================================
-- Функції для автоматичного синхронізування даних
-- ============================================

-- Функція для синхронізації JSONB з колонками при INSERT/UPDATE travel_plans
CREATE OR REPLACE FUNCTION sync_travel_plan_data()
RETURNS TRIGGER AS $$
DECLARE
    columns_changed BOOLEAN;
BEGIN
    -- Перевіряємо, чи змінилися окремі колонки
    columns_changed := (
        TG_OP = 'INSERT' OR 
        (TG_OP = 'UPDATE' AND (
            OLD.title IS DISTINCT FROM NEW.title OR
            OLD.description IS DISTINCT FROM NEW.description OR
            OLD.start_date IS DISTINCT FROM NEW.start_date OR
            OLD.end_date IS DISTINCT FROM NEW.end_date OR
            OLD.budget IS DISTINCT FROM NEW.budget OR
            OLD.currency IS DISTINCT FROM NEW.currency OR
            OLD.is_public IS DISTINCT FROM NEW.is_public
        ))
    );
    
    -- Якщо змінилися окремі колонки, оновлюємо JSONB
    -- (Пріоритет завжди на окремих колонках, якщо вони змінюються)
    IF columns_changed THEN
        NEW.data = jsonb_build_object(
            'title', NEW.title,
            'description', NEW.description,
            'start_date', CASE WHEN NEW.start_date IS NULL THEN NULL ELSE NEW.start_date::text END,
            'end_date', CASE WHEN NEW.end_date IS NULL THEN NULL ELSE NEW.end_date::text END,
            'budget', CASE WHEN NEW.budget IS NULL THEN NULL ELSE NEW.budget::text END,
            'currency', COALESCE(NEW.currency, 'USD'),
            'is_public', COALESCE(NEW.is_public, false)
        );
    -- Якщо змінився ТІЛЬКИ JSONB (а не окремі колонки), оновлюємо окремі колонки
    ELSIF TG_OP = 'UPDATE' AND OLD.data IS DISTINCT FROM NEW.data AND NOT columns_changed THEN
        NEW.title = COALESCE(NEW.data->>'title', NEW.title);
        NEW.description = CASE WHEN NEW.data->>'description' IS NULL THEN NULL ELSE NEW.data->>'description' END;
        NEW.start_date = CASE WHEN NEW.data->>'start_date' IS NULL OR NEW.data->>'start_date' = '' THEN NULL ELSE (NEW.data->>'start_date')::date END;
        NEW.end_date = CASE WHEN NEW.data->>'end_date' IS NULL OR NEW.data->>'end_date' = '' THEN NULL ELSE (NEW.data->>'end_date')::date END;
        NEW.budget = CASE WHEN NEW.data->>'budget' IS NULL OR NEW.data->>'budget' = '' THEN NULL ELSE (NEW.data->>'budget')::numeric(10,2) END;
        NEW.currency = COALESCE(NEW.data->>'currency', 'USD');
        NEW.is_public = COALESCE((NEW.data->>'is_public')::boolean, false);
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Функція для синхронізації JSONB з колонками при INSERT/UPDATE locations
CREATE OR REPLACE FUNCTION sync_location_data()
RETURNS TRIGGER AS $$
DECLARE
    columns_changed BOOLEAN;
BEGIN
    -- Перевіряємо, чи змінилися окремі колонки
    columns_changed := (
        TG_OP = 'INSERT' OR 
        (TG_OP = 'UPDATE' AND (
            OLD.name IS DISTINCT FROM NEW.name OR
            OLD.address IS DISTINCT FROM NEW.address OR
            OLD.latitude IS DISTINCT FROM NEW.latitude OR
            OLD.longitude IS DISTINCT FROM NEW.longitude OR
            OLD.visit_order IS DISTINCT FROM NEW.visit_order OR
            OLD.arrival_date IS DISTINCT FROM NEW.arrival_date OR
            OLD.departure_date IS DISTINCT FROM NEW.departure_date OR
            OLD.budget IS DISTINCT FROM NEW.budget OR
            OLD.notes IS DISTINCT FROM NEW.notes
        ))
    );
    
    -- Якщо змінилися окремі колонки, оновлюємо JSONB
    -- (Пріоритет завжди на окремих колонках, якщо вони змінюються)
    IF columns_changed THEN
        NEW.data = jsonb_build_object(
            'name', NEW.name,
            'address', NEW.address,
            'latitude', CASE WHEN NEW.latitude IS NULL THEN NULL ELSE NEW.latitude::text END,
            'longitude', CASE WHEN NEW.longitude IS NULL THEN NULL ELSE NEW.longitude::text END,
            'visit_order', NEW.visit_order,
            'arrival_date', CASE WHEN NEW.arrival_date IS NULL THEN NULL ELSE NEW.arrival_date::text END,
            'departure_date', CASE WHEN NEW.departure_date IS NULL THEN NULL ELSE NEW.departure_date::text END,
            'budget', CASE WHEN NEW.budget IS NULL THEN NULL ELSE NEW.budget::text END,
            'notes', NEW.notes
        );
    -- Якщо змінився ТІЛЬКИ JSONB (а не окремі колонки), оновлюємо окремі колонки
    ELSIF TG_OP = 'UPDATE' AND OLD.data IS DISTINCT FROM NEW.data AND NOT columns_changed THEN
        NEW.name = COALESCE(NEW.data->>'name', NEW.name);
        NEW.address = CASE WHEN NEW.data->>'address' IS NULL OR NEW.data->>'address' = '' THEN NULL ELSE NEW.data->>'address' END;
        NEW.latitude = CASE WHEN NEW.data->>'latitude' IS NULL OR NEW.data->>'latitude' = '' THEN NULL ELSE (NEW.data->>'latitude')::numeric(10,6) END;
        NEW.longitude = CASE WHEN NEW.data->>'longitude' IS NULL OR NEW.data->>'longitude' = '' THEN NULL ELSE (NEW.data->>'longitude')::numeric(11,6) END;
        NEW.visit_order = CASE WHEN NEW.data->>'visit_order' IS NULL THEN NULL ELSE (NEW.data->>'visit_order')::integer END;
        NEW.arrival_date = CASE WHEN NEW.data->>'arrival_date' IS NULL OR NEW.data->>'arrival_date' = '' THEN NULL ELSE (NEW.data->>'arrival_date')::timestamptz END;
        NEW.departure_date = CASE WHEN NEW.data->>'departure_date' IS NULL OR NEW.data->>'departure_date' = '' THEN NULL ELSE (NEW.data->>'departure_date')::timestamptz END;
        NEW.budget = CASE WHEN NEW.data->>'budget' IS NULL OR NEW.data->>'budget' = '' THEN NULL ELSE (NEW.data->>'budget')::numeric(10,2) END;
        NEW.notes = CASE WHEN NEW.data->>'notes' IS NULL OR NEW.data->>'notes' = '' THEN NULL ELSE NEW.data->>'notes' END;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Створюємо тригери для автоматичної синхронізації
DROP TRIGGER IF EXISTS sync_travel_plan_data_trigger ON travel_plans;
CREATE TRIGGER sync_travel_plan_data_trigger
    BEFORE INSERT OR UPDATE ON travel_plans
    FOR EACH ROW
    EXECUTE FUNCTION sync_travel_plan_data();

DROP TRIGGER IF EXISTS sync_location_data_trigger ON locations;
CREATE TRIGGER sync_location_data_trigger
    BEFORE INSERT OR UPDATE ON locations
    FOR EACH ROW
    EXECUTE FUNCTION sync_location_data();

