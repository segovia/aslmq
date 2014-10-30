

CREATE OR REPLACE FUNCTION select_synced_middle_time () 
RETURNS SETOF middle_time AS
'
DECLARE
	min_query_start timestamp;
BEGIN
	
	SELECT min(db_query_start) INTO min_query_start FROM experiment;
	RETURN QUERY SELECT m.id,
						m.experiment_id,
						m.client_id,
						m.elapsed_time + CAST(EXTRACT(microsecond from e.db_query_start - min_query_start) * 1000 AS BIGINT) as synced_elapsed_time,
						m.response_time,
						m.request_type,
						m.response_type,
						m.serialization_time,
						m.deserialization_time,
						m.acquire_db_connection_time,
						m.release_db_connection_time,
						m.database_network_time,
						m.statement_exec_time,
						m.free_db_connections,
						m.created_db_connections
				   FROM middle_time m, experiment e
				  WHERE m.experiment_id = e.id
			   ORDER BY synced_elapsed_time;
END;
'
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION select_synced_client_time () 
RETURNS SETOF client_time AS
'
DECLARE
	min_query_start timestamp;
BEGIN
	SELECT min(db_query_start) INTO min_query_start FROM experiment;
	RETURN QUERY SELECT c.id,
						c.experiment_id,
						c.client_id,
						c.elapsed_time + CAST(EXTRACT(microsecond from e.db_query_start - min_query_start) * 1000 AS BIGINT) as synced_elapsed_time,
						c.response_time,
						c.request_type,
						c.response_type,
						c.serialization_time,
						c.deserialization_time,
						c.network_time
				   FROM client_time c, experiment e
				  WHERE c.experiment_id = e.id
			   ORDER BY synced_elapsed_time;
END;
'
LANGUAGE plpgsql;

