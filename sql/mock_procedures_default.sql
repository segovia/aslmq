CREATE TYPE status_result as (elapsed INT, status INT);
CREATE TYPE create_queue_result as (elapsed INT, queue_id INT);
CREATE TYPE message_read_result as (elapsed INT, status INT, message_id INT, sender_id INT, recipient_id INT, queue_id INT, message_text TEXT);

CREATE OR REPLACE FUNCTION elapsed_time (start timestamptz)
RETURNS INTEGER AS
'
BEGIN
   	RETURN CAST((extract(epoch from (clock_timestamp()-start)))*1000 AS INTEGER);
END;
'
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION check_account (i_account_id INT)
RETURNS status_result AS
'
DECLARE
	start timestamptz;
	result status_result;
BEGIN
	start := clock_timestamp();
	result.status := 0;
	result.elapsed = elapsed_time(start);
   	RETURN result;
END;
'
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION create_queue ()
RETURNS create_queue_result AS
'
DECLARE
	start timestamptz;
	result create_queue_result;
BEGIN
	start := clock_timestamp();
	result.queue_id := 1;
	result.elapsed = elapsed_time(start);
   	RETURN result;
END;
'
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_queue (i_queue_id INT)
RETURNS status_result AS
'
DECLARE
	start timestamptz;
	deleted_rows INT;
	result status_result;
BEGIN
	start := clock_timestamp();
	result.status := 0; -- OK
	result.elapsed = elapsed_time(start);
   	RETURN result;
END;
'
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION write_message (i_sender_id INT, i_queue_id INT, i_message_text TEXT, i_recipient_id INT)
RETURNS status_result AS
'
DECLARE
	start timestamptz;
	result status_result;
BEGIN
	start := clock_timestamp();
	result.status = 0;
	result.elapsed = elapsed_time(start);
   	RETURN result;
END;
'
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION select_message (i_recipient_id INT, i_sender_id INT, i_queue_id INT) 
RETURNS INT AS
'
BEGIN
	RETURN 0;
END;
'
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION read_message (i_recipient_id INT, i_sender_id INT, i_queue_id INT, i_only_peek BOOLEAN) 
RETURNS message_read_result AS
'
DECLARE
	start timestamptz;
	result message_read_result;
BEGIN
	start := clock_timestamp();
	result.status = 0;
	result.message_id = 1;
	result.sender_id = 1;
	result.recipient_id = 1;
	result.queue_id = 1;
	result.message_text = ''Msg 200 chars 0123456789 Msg 200 chars 0123456789 Msg 200 chars 0123456789 Msg 200 chars 0123456789 Msg 200 chars 0123456789 Msg 200 chars 0123456789 Msg 200 chars 0123456789 Msg 200 chars 0123456789 '';
	result.elapsed := elapsed_time(start);
   	RETURN result;
END;
'
LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION find_queues_with_message (i_client_id INT) 
RETURNS SETOF INT AS
'
DECLARE
	start timestamptz;
	row INT;
BEGIN
	start := clock_timestamp();
	RETURN NEXT 1;
	RETURN NEXT elapsed_time(start);
	RETURN;
END;
'
LANGUAGE plpgsql;


-- create 256 clients and 30 queues
CREATE OR REPLACE FUNCTION initial_data () 
RETURNS INT AS
'
BEGIN
	FOR i IN 1..30 LOOP
		INSERT INTO queue VALUES(default);
	END LOOP;
	FOR i IN 1..256 LOOP
		INSERT INTO account VALUES(default);
	END LOOP;
	RETURN 0;
END;
'
LANGUAGE plpgsql;