BEGIN;

ALTER TABLE travel_plans ADD COLUMN IF NOT EXISTS data JSONB;

UPDATE travel_plans tp
SET data = jsonb_build_object(
        'id', tp.id,
        'title', tp.title,
        'description', tp.description,
        'is_public', COALESCE(tp.is_public, false),

        'dates', jsonb_build_object(
                'start', tp.start_date,
                'end', tp.end_date
                 ),

        'budget_info', jsonb_build_object(
                'amount', tp.budget,
                'currency', COALESCE(tp.currency, 'USD')
                       ),

        'meta', jsonb_build_object(
                'version', tp.version,
                'created_at', tp.created_at,
                'updated_at', tp.updated_at
                ),

        'locations', COALESCE(
                (
                    SELECT jsonb_agg(
                                   jsonb_build_object(
                                           'id', l.id,
                                           'name', l.name,
                                           'address', l.address,
                                           'visit_order', l.visit_order,
                                           'coordinates', jsonb_build_object(
                                                   'lat', l.latitude,
                                                   'lng', l.longitude
                                                          ),
                                           'timing', jsonb_build_object(
                                                   'arrival', l.arrival_date,
                                                   'departure', l.departure_date
                                                     ),
                                           'budget', l.budget,
                                           'notes', l.notes,
                                           'created_at', l.created_at
                                   ) ORDER BY l.visit_order ASC
                           )
                    FROM public.locations l
                    WHERE l.travel_plan_id = tp.id
                ),
                '[]'::jsonb
                     )
           );


CREATE INDEX idx_travel_plans_data ON travel_plans USING GIN (data);

DROP TABLE IF EXISTS locations;

ALTER TABLE travel_plans
DROP COLUMN IF EXISTS title,
    DROP COLUMN IF EXISTS description,
    DROP COLUMN IF EXISTS start_date,
    DROP COLUMN IF EXISTS end_date,
    DROP COLUMN IF EXISTS budget,
    DROP COLUMN IF EXISTS currency,
    DROP COLUMN IF EXISTS is_public,
    DROP COLUMN IF EXISTS created_at,
    DROP COLUMN IF EXISTS updated_at;

COMMIT;