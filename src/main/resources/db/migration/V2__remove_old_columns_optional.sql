-- ОПЦІОНАЛЬНА МІГРАЦІЯ: Видалення старих колонок після повного переходу на JSONB
-- УВАГА: Цю міграцію слід використовувати ТІЛЬКИ після того, як весь код буде оновлений
-- для роботи з JSONB колонками. Інакше застосунок перестане працювати!
--
-- Для активації цієї міграції:
-- 1. Перейменуйте файл, прибравши "_optional" з назви
-- 2. Переконайтеся, що весь код використовує JSONB колонки
-- 3. Зробіть резервну копію бази даних
-- 4. Запустіть міграцію

-- ============================================
-- TRAVEL PLANS: Видалення старих колонок
-- ============================================

-- Спочатку видаляємо тригери синхронізації
DROP TRIGGER IF EXISTS sync_travel_plan_data_trigger ON travel_plans;
DROP FUNCTION IF EXISTS sync_travel_plan_data();

-- Видаляємо старі колонки (залишаємо тільки id, version, created_at, updated_at, data, metadata)
-- ALTER TABLE travel_plans DROP COLUMN IF EXISTS title;
-- ALTER TABLE travel_plans DROP COLUMN IF EXISTS description;
-- ALTER TABLE travel_plans DROP COLUMN IF EXISTS start_date;
-- ALTER TABLE travel_plans DROP COLUMN IF EXISTS end_date;
-- ALTER TABLE travel_plans DROP COLUMN IF EXISTS budget;
-- ALTER TABLE travel_plans DROP COLUMN IF EXISTS currency;
-- ALTER TABLE travel_plans DROP COLUMN IF EXISTS is_public;

-- Видаляємо старі індекси, які більше не потрібні
-- DROP INDEX IF EXISTS idx_travel_plans_dates;
-- DROP INDEX IF EXISTS idx_travel_plans_public;

-- Видаляємо старі constraints
-- ALTER TABLE travel_plans DROP CONSTRAINT IF EXISTS check_dates;
-- ALTER TABLE travel_plans DROP CONSTRAINT IF EXISTS check_budget;

-- ============================================
-- LOCATIONS: Видалення старих колонок
-- ============================================

-- Спочатку видаляємо тригери синхронізації
DROP TRIGGER IF EXISTS sync_location_data_trigger ON locations;
DROP FUNCTION IF EXISTS sync_location_data();

-- Видаляємо старі колонки (залишаємо тільки id, travel_plan_id, version, created_at, data)
-- ALTER TABLE locations DROP COLUMN IF EXISTS name;
-- ALTER TABLE locations DROP COLUMN IF EXISTS address;
-- ALTER TABLE locations DROP COLUMN IF EXISTS latitude;
-- ALTER TABLE locations DROP COLUMN IF EXISTS longitude;
-- ALTER TABLE locations DROP COLUMN IF EXISTS visit_order;
-- ALTER TABLE locations DROP COLUMN IF EXISTS arrival_date;
-- ALTER TABLE locations DROP COLUMN IF EXISTS departure_date;
-- ALTER TABLE locations DROP COLUMN IF EXISTS budget;
-- ALTER TABLE locations DROP COLUMN IF EXISTS notes;

-- Видаляємо старі індекси
-- DROP INDEX IF EXISTS idx_locations_plan_order;
-- DROP INDEX IF EXISTS idx_locations_coordinates;

-- Видаляємо старі constraints
-- ALTER TABLE locations DROP CONSTRAINT IF EXISTS check_coordinates_lat;
-- ALTER TABLE locations DROP CONSTRAINT IF EXISTS check_coordinates_lng;
-- ALTER TABLE locations DROP CONSTRAINT IF EXISTS check_location_dates;
-- ALTER TABLE locations DROP CONSTRAINT IF EXISTS check_location_budget;
-- ALTER TABLE locations DROP CONSTRAINT IF EXISTS unique_plan_order;

-- Видаляємо старий тригер для visit_order
-- DROP TRIGGER IF EXISTS auto_assign_location_order ON locations;
-- DROP FUNCTION IF EXISTS assign_location_order();

-- ============================================
-- НОВІ CONSTRAINTS ДЛЯ JSONB
-- ============================================

-- Додаємо constraints для JSONB полів через CHECK
-- Для travel_plans
ALTER TABLE travel_plans 
ADD CONSTRAINT check_data_title 
CHECK (data->>'title' IS NOT NULL AND LENGTH(TRIM(data->>'title')) > 0);

ALTER TABLE travel_plans 
ADD CONSTRAINT check_data_currency 
CHECK (data->>'currency' IS NULL OR LENGTH(data->>'currency') = 3);

ALTER TABLE travel_plans 
ADD CONSTRAINT check_data_dates 
CHECK (
    data->>'end_date' IS NULL OR 
    data->>'start_date' IS NULL OR 
    (data->>'end_date')::date >= (data->>'start_date')::date
);

ALTER TABLE travel_plans 
ADD CONSTRAINT check_data_budget 
CHECK (data->>'budget' IS NULL OR (data->>'budget')::numeric >= 0);

-- Для locations
ALTER TABLE locations 
ADD CONSTRAINT check_data_name 
CHECK (data->>'name' IS NOT NULL AND LENGTH(TRIM(data->>'name')) > 0);

ALTER TABLE locations 
ADD CONSTRAINT check_data_coordinates_lat 
CHECK (
    data->>'latitude' IS NULL OR 
    ((data->>'latitude')::numeric BETWEEN -90 AND 90)
);

ALTER TABLE locations 
ADD CONSTRAINT check_data_coordinates_lng 
CHECK (
    data->>'longitude' IS NULL OR 
    ((data->>'longitude')::numeric BETWEEN -180 AND 180)
);

ALTER TABLE locations 
ADD CONSTRAINT check_data_dates 
CHECK (
    data->>'departure_date' IS NULL OR 
    data->>'arrival_date' IS NULL OR 
    (data->>'departure_date')::timestamptz >= (data->>'arrival_date')::timestamptz
);

ALTER TABLE locations 
ADD CONSTRAINT check_data_budget 
CHECK (data->>'budget' IS NULL OR (data->>'budget')::numeric >= 0);

-- ============================================
-- НОВІ ІНДЕКСИ ДЛЯ JSONB
-- ============================================

-- Індекси для швидкого пошуку по полям в JSONB
CREATE INDEX IF NOT EXISTS idx_travel_plans_data_title ON travel_plans ((data->>'title'));
CREATE INDEX IF NOT EXISTS idx_travel_plans_data_dates ON travel_plans 
    (((data->>'start_date')::date), ((data->>'end_date')::date)) 
    WHERE data->>'start_date' IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_travel_plans_data_public ON travel_plans 
    (((data->>'is_public')::boolean), updated_at DESC) 
    WHERE (data->>'is_public')::boolean = true;

CREATE INDEX IF NOT EXISTS idx_locations_data_name ON locations ((data->>'name'));
CREATE INDEX IF NOT EXISTS idx_locations_data_order ON locations 
    (travel_plan_id, ((data->>'visit_order')::integer));
CREATE INDEX IF NOT EXISTS idx_locations_data_coordinates ON locations 
    (((data->>'latitude')::numeric), ((data->>'longitude')::numeric)) 
    WHERE data->>'latitude' IS NOT NULL AND data->>'longitude' IS NOT NULL;

