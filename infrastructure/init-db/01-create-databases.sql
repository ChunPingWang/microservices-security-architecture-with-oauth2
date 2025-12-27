-- Create databases for each microservice
CREATE DATABASE customer_db;
CREATE DATABASE product_db;
CREATE DATABASE order_db;
CREATE DATABASE payment_db;
CREATE DATABASE logistics_db;
CREATE DATABASE sales_db;
CREATE DATABASE admin_db;

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE customer_db TO ecommerce;
GRANT ALL PRIVILEGES ON DATABASE product_db TO ecommerce;
GRANT ALL PRIVILEGES ON DATABASE order_db TO ecommerce;
GRANT ALL PRIVILEGES ON DATABASE payment_db TO ecommerce;
GRANT ALL PRIVILEGES ON DATABASE logistics_db TO ecommerce;
GRANT ALL PRIVILEGES ON DATABASE sales_db TO ecommerce;
GRANT ALL PRIVILEGES ON DATABASE admin_db TO ecommerce;
