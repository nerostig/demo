CREATE TABLE IF NOT EXISTS topologies (
id SERIAL PRIMARY KEY,
name TEXT
);

-- =========================
-- SENSORS (corrigido)
-- =========================
CREATE TABLE IF NOT EXISTS sensors (
id TEXT NOT NULL,
topology_id INT NOT NULL REFERENCES topologies(id) ON DELETE CASCADE,
group_id TEXT,
x INT,
y INT,
desired_duty_cycle DOUBLE PRECISION,
tolerance DOUBLE PRECISION NOT NULL,

PRIMARY KEY (topology_id, id)
);

-- =========================
-- EDGES
-- =========================
CREATE TABLE IF NOT EXISTS edges (
topology_id INT NOT NULL,
source_id TEXT NOT NULL,
target_id TEXT NOT NULL,

FOREIGN KEY (topology_id, source_id)
REFERENCES sensors(topology_id, id) ON DELETE CASCADE,

FOREIGN KEY (topology_id, target_id)
REFERENCES sensors(topology_id, id) ON DELETE CASCADE,

PRIMARY KEY (topology_id, source_id, target_id)
);

-- =========================
-- DUTY CYCLES
-- =========================
CREATE TABLE IF NOT EXISTS duty_cycles (
topology_id INT NOT NULL,
sensor_id TEXT NOT NULL,
value DOUBLE PRECISION,

FOREIGN KEY (topology_id, sensor_id)
REFERENCES sensors(topology_id, id) ON DELETE CASCADE,

PRIMARY KEY (topology_id, sensor_id)
);