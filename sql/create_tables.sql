CREATE TABLE queue (
	id SERIAL PRIMARY KEY
	);	
	
CREATE TABLE account (
	id SERIAL PRIMARY KEY
	);
	
CREATE TABLE message (
	id BIGSERIAL PRIMARY KEY,
	sender_id INTEGER,
	recipient_id INTEGER,
	queue_id INTEGER REFERENCES queue ON DELETE CASCADE, 
	text TEXT,
	arrival TIMESTAMP default current_timestamp
	);

CREATE INDEX message_idx_queue_id ON message (queue_id);
CREATE INDEX message_idx_sender_id ON message (sender_id);