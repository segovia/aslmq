CREATE TYPE status_result as (elapsed BIGINT, status INT);
CREATE TYPE create_queue_result as (elapsed BIGINT, queue_id INT);
CREATE TYPE message_read_result as (elapsed BIGINT, status INT, message_id INT, sender_id INT, recipient_id INT, queue_id INT, message_text TEXT, arrival TIMESTAMP);

CREATE OR REPLACE FUNCTION elapsed_time (start timestamptz)
RETURNS BIGINT AS
'
BEGIN
   	RETURN CAST((extract(seconds from (clock_timestamp()-start)))*1000000000 AS BIGINT);
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
	SELECT count(id) INTO result.status FROM account WHERE id = i_account_id;
	IF result.status = 1 THEN
		result.status := 0;
	ELSE
		result.status := -1;
	END IF;
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
	INSERT INTO queue values(default) RETURNING id INTO result.queue_id;
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
	DELETE FROM queue WHERE id = i_queue_id;
	GET DIAGNOSTICS deleted_rows := ROW_COUNT;
	IF deleted_rows = 0 THEN
		result.status := -1; -- queue doesnt exist
	ELSE
		result.status := 0; -- OK
	END IF;
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
	IF i_recipient_id IS NULL THEN
		INSERT INTO message (sender_id, queue_id, text) VALUES (i_sender_id, i_queue_id, i_message_text);
	ELSE
		INSERT INTO message (sender_id, queue_id, text, recipient_id) VALUES (i_sender_id, i_queue_id, i_message_text, i_recipient_id);
	END IF;
	result.status = 0;
	result.elapsed = elapsed_time(start);
   	RETURN result;
EXCEPTION
	WHEN foreign_key_violation THEN 
		result.status = -1;
		result.elapsed = elapsed_time(start);
	   	RETURN result;
END;
'
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION select_message (i_recipient_id INT, i_sender_id INT, i_queue_id INT) 
RETURNS TABLE(o_message_id BIGINT, o_sender_id INT, o_recipient_id INT, o_queue_id INT, o_message_text TEXT, o_arrival TIMESTAMP, o_rowId TID) AS
'
DECLARE
	result message_read_result;
BEGIN
	IF i_sender_id IS NOT NULL AND i_queue_id IS NOT NULL THEN
		RETURN QUERY SELECT id, sender_id, recipient_id, queue_id, text, arrival, ctid FROM message where queue_id = i_queue_id and 	sender_id = i_sender_id and (recipient_id is null or recipient_id = i_recipient_id) ORDER BY id ASC LIMIT 1;
	ELSEIF i_sender_id IS NULL AND i_queue_id IS NOT NULL THEN
		RETURN QUERY SELECT id, sender_id, recipient_id, queue_id, text, arrival, ctid FROM message where queue_id = i_queue_id and 								(recipient_id is null or recipient_id = i_recipient_id) ORDER BY id ASC LIMIT 1;
	ELSE
		RETURN QUERY SELECT id, sender_id, recipient_id, queue_id, text, arrival, ctid FROM message where 							sender_id = i_sender_id and (recipient_id is null or recipient_id = i_recipient_id) ORDER BY id ASC LIMIT 1;
	END IF;
END;
'
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION read_message (i_recipient_id INT, i_sender_id INT, i_queue_id INT, i_only_peek BOOLEAN) 
RETURNS message_read_result AS
'
DECLARE
	start timestamptz;
	deleted_rows INT;
	selected_rows INT;
	count INT;
	result message_read_result;
BEGIN
	start := clock_timestamp();
	IF i_sender_id IS NULL AND i_queue_id IS NULL THEN
		result.status = -3;
	ELSE
		IF i_only_peek THEN
			SELECT * INTO result.message_id, result.sender_id, result.recipient_id, result.queue_id, result.message_text, result.arrival FROM select_message(i_recipient_id, i_sender_id, i_queue_id);
			result.status := 0;
			GET DIAGNOSTICS selected_rows := ROW_COUNT;
			IF selected_rows = 0 THEN
				IF i_queue_id IS NULL THEN
					result.status := -4; -- no message found matching query
				ELSE
					-- either queue does not exist or is empty
					SELECT count(id) INTO count FROM queue where id = i_queue_id;
					IF count = 0 THEN
						result.status := -1; -- queue doesnt exist
					ELSE
						result.status := -2; -- queue is empty
					END IF;
				END IF;
			END IF;
		ELSE
			LOOP
				-- blocks if other delete is happening
				DELETE FROM message WHERE ctid IN (SELECT o_rowId FROM select_message(i_recipient_id, i_sender_id, i_queue_id)) RETURNING id, sender_id, recipient_id, queue_id, text, arrival INTO result.message_id, result.sender_id, result.recipient_id, result.queue_id, result.message_text, result.arrival;
				result.status := 0;
				GET DIAGNOSTICS deleted_rows := ROW_COUNT;
			    EXIT WHEN deleted_rows > 0;
			
				-- nothing was deleted. This could be caused by 2 reasons
				-- another delete stmt deleted target row or
				-- simply there are no target rows (which might be caused by non existing queue or empty queue)
				IF i_queue_id IS NOT NULL THEN
					SELECT count(*) INTO count FROM queue where id = i_queue_id;
					IF count = 0 THEN
						result.status = -1; -- queue doesnt exist
						EXIT;
					END IF;
				END IF;
		
				SELECT count(*) INTO count FROM select_message(i_recipient_id, i_sender_id, i_queue_id);
				IF count = 0 THEN
					IF i_queue_id IS NOT NULL THEN
						result.status = -2; -- queue is empty
					ELSE
						result.status := -4; -- no message found matching query
					END IF;
					EXIT;
				END IF;
				-- there is something still on the queue, try again
			END LOOP;
		END IF;
	END IF;
	result.elapsed := elapsed_time(start);
   	RETURN result;
END;
'
LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION find_queues_with_message (i_client_id INT) 
RETURNS SETOF BIGINT AS
'
DECLARE
	start timestamptz;
	row INT;
BEGIN
	start := clock_timestamp();
	FOR row in SELECT DISTINCT(queue_id) FROM message where recipient_id is null or recipient_id = i_client_id ORDER BY queue_id ASC LOOP
		RETURN NEXT row;
	END LOOP;
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

-- create 256 clients and 30 queues
CREATE OR REPLACE FUNCTION fill_db () 
RETURNS INT AS
'
BEGIN
	FOR k IN 1..10000 LOOP
		FOR j IN 1..30 LOOP
			FOR i IN 1..10 LOOP
				-- INSERT INTO message (sender_id, queue_id, text, recipient_id) VALUES (i_sender_id, i_queue_id, i_message_text, i_recipient_id);
				INSERT INTO message (sender_id, queue_id, text) VALUES (j, i, ''This is the message text'');
				INSERT INTO message (sender_id, queue_id, text, recipient_id) VALUES (j, i, ''This is the message text'', j);
			END LOOP;
		END LOOP;
	END LOOP;
	RETURN 0;
END;
'
LANGUAGE plpgsql;
