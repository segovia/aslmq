python python/connections_graph.py measurements/database_perf/01_database_performance_baseline_consolidated_ "Baseline Database Performance X Number of Connections"
python python/connections_graph.py measurements/database_perf/01_database_performance_large_consolidated_ "Large Database Performance X Number of Connections"

python python/connections_graph.py measurements/database_network/01_database_time_amazon_network_consolidated_ "Database Network Performance X Number of Connections (Small Message)" network_db
python python/connections_graph.py measurements/database_network/01_database_time_amazon_network_large_consolidated_ "Database Network Performance X Number of Connections (Large Message)" network_db

python python/connections_graph.py measurements/network/01_network_consolidated_ "Network Performance X Number of Connections" network

python python/system_trace_graph.py measurements/system_3_factor/01_2_mid_08_conn_norm_db_

python python/x_factor_graph.py measurements/msg_size_x_dataset/01_msg_size_x_dataset_consolidated_ "Message Size x Dataset" "msg_size_x_dataset"
python python/x_factor_graph.py measurements/system_3_factor/01_system_3_factor_consolidated_ "Middleware Count X Database Connections X Database Instance Size" "system_3_factor"

python python/micro_benchmark_database_graph.py measurements/database_perf/01_database_performance_consolidated_micro_benchmark_baseline.csv measurements/database_perf/01_database_performance_consolidated_micro_benchmark_large.csv

python python/client_number_graph.py measurements/system_client_number/01_system_client_number_consolidated_ "System Performance X Number of Clients"

python python/micro_benchmark_client_number_graph.py measurements/system_client_number/01_system_client_number_per_request_type_consolidated_response_time.csv