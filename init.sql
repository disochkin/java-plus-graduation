CREATE DATABASE main_service_db;
CREATE DATABASE stat_service_db;
CREATE DATABASE user_service_db;
CREATE DATABASE event_service_db;
CREATE DATABASE request_service_db;
CREATE DATABASE comment_service_db;

GRANT ALL PRIVILEGES ON DATABASE main_service_db TO ewmuser;
GRANT ALL PRIVILEGES ON DATABASE stat_service_db TO ewmuser;
GRANT ALL PRIVILEGES ON DATABASE user_service_db TO ewmuser;
GRANT ALL PRIVILEGES ON DATABASE event_service_db TO ewmuser;
GRANT ALL PRIVILEGES ON DATABASE request_service_db TO ewmuser;
GRANT ALL PRIVILEGES ON DATABASE comment_service_db TO ewmuser;




