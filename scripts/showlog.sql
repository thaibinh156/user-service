

SET global general_log = on;
SET GLOBAL general_log_file = '/var/lib/mysql/general.log';
SET global log_output = 'TABLE';



SELECT event_time, argument  FROM mysql.general_log 
WHERE ( argument LIKE 'INSERT%' 
   OR argument LIKE 'UPDATE%' 
   OR argument LIKE 'DELETE%' 
   OR argument LIKE 'SELECT%')
 AND event_time > '2025-02-03 04:04:17'  and event_time < '2025-02-03 04:04:30'
 and command_type = 'Execute'
ORDER BY event_time DESC;

SHOW VARIABLES LIKE 'general_log';
SHOW VARIABLES LIKE 'log_output';
SHOW VARIABLES LIKE 'general_log_file';

-- 1
SELECT * FROM mysql_migration_version

-- 2
SELECT MAX(id) FROM relation_tuple_transaction LIMIT 1

-- 3
SELECT unique_id FROM mysql_metadata

-- 4
SELECT UTC_TIMESTAMP(6)

-- 5
SELECT MAX(id) FROM relation_tuple_transaction WHERE timestamp < ? LIMIT 1

-- 6 
SELECT MAX(id) FROM relation_tuple_transaction WHERE timestamp < '2025-02-02 03:16:17.088007' LIMIT 1

-- 7
DELETE FROM relation_tuple WHERE deleted_transaction <= ? LIMIT 1000

-- 8
DELETE FROM relation_tuple WHERE deleted_transaction <= ? LIMIT 1000

SELECT COALESCE((
			SELECT MIN(id)
			FROM   relation_tuple_transaction
			WHERE  timestamp >= FROM_UNIXTIME(FLOOR(UNIX_TIMESTAMP(UTC_TIMESTAMP(6)) * 1000000000 / 5000000000) * 5000000000 / 1000000000)
		), (
			SELECT MAX(id)
			FROM   relation_tuple_transaction
		)) as revision,
		5000000000 - CAST(UNIX_TIMESTAMP(UTC_TIMESTAMP(6)) * 1000000000 AS UNSIGNED INTEGER) % 5000000000 as validForNanos

explain SELECT namespace, object_id, relation, userset_namespace, userset_object_id, userset_relation, caveat_name, caveat_context FROM relation_tuple WHERE created_transaction <= 12 AND (deleted_transaction = 9223372036854775807 OR deleted_transaction > 12) AND namespace = 'folder' AND relation = 'owner' AND object_id IN ('folder_RD') AND ((userset_namespace = 'user' AND userset_object_id IN ('RD_Lead') AND userset_relation = '...')) LIMIT 9223372036854775807

Delete form