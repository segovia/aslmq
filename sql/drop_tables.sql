DROP INDEX message_idx_queue_id;
DROP INDEX message_idx_sender_id;

DROP FUNCTION initial_data ();

DROP FUNCTION check_account (INT);
DROP FUNCTION select_message (INT, INT, INT);
DROP FUNCTION create_queue ();
DROP FUNCTION delete_queue (INT);
DROP FUNCTION read_message (INT, INT, INT, BOOLEAN);
DROP FUNCTION write_message (INT,INT,TEXT,INT);
DROP FUNCTION find_queues_with_message (INT);
DROP FUNCTION elapsed_time (timestamptz);
DROP TYPE message_read_result CASCADE;
DROP TYPE create_queue_result CASCADE;
DROP TYPE status_result CASCADE;

DROP TABLE message;
DROP TABLE account;
DROP TABLE queue;