ALTER TABLE drivers ADD COLUMN IF NOT EXISTS custom_data JSONB;
ALTER TABLE fleets ADD COLUMN IF NOT EXISTS custom_data JSONB;
ALTER TABLE carriers ADD COLUMN IF NOT EXISTS custom_data JSONB;
ALTER TABLE transport_shipments ADD COLUMN IF NOT EXISTS custom_data JSONB;
ALTER TABLE transport_requests ADD COLUMN IF NOT EXISTS custom_data JSONB;
ALTER TABLE ready_to_ship_orders ADD COLUMN IF NOT EXISTS custom_data JSONB;
