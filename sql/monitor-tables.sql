
DROP FUNCTION select_synced_middle_time();
DROP FUNCTION select_synced_client_time();

DROP TABLE database_time;
DROP TABLE client_time;
DROP TABLE middle_time;
DROP TABLE experiment;

CREATE TABLE experiment (
	id BIGSERIAL PRIMARY KEY,
	db_query_start TIMESTAMP default current_timestamp,
	jvm_startup TIMESTAMP,
	jvm_query_start TIMESTAMP,
	start_error BIGINT
	);
	
	
CREATE TABLE client_time (
	id BIGSERIAL PRIMARY KEY,
	experiment_id INTEGER,
	client_id INTEGER,
	elapsed_time BIGINT,
	response_time BIGINT,
	request_type INTEGER,
	response_type INTEGER,
	serialization_time BIGINT,
	deserialization_time BIGINT,
	network_time BIGINT
	);
	

	
CREATE TABLE middle_time (
	id BIGSERIAL PRIMARY KEY,
	experiment_id INTEGER,
	client_id INTEGER,
	elapsed_time BIGINT,
	response_time BIGINT,
	request_type INTEGER,
	response_type INTEGER,
	serialization_time BIGINT,
	deserialization_time BIGINT,
	acquire_db_connection_time BIGINT,
	release_db_connection_time BIGINT,
	database_network_time BIGINT,
	statement_exec_time BIGINT,
	free_db_connections INTEGER,
	created_db_connections INTEGER
	);
	
CREATE TABLE database_time (
	id BIGSERIAL PRIMARY KEY,
	experiment_id INTEGER,
	client_id INTEGER,
	elapsed_time BIGINT,
	database_network_time BIGINT,
	request_type INTEGER,
	response_type INTEGER,
	statement_exec_time BIGINT
	);