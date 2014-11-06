python python/connections_graph.py measurements/database_perf/01_database_performance_baseline_consolidated_ "Baseline Database Performance X Number of Connections"
python python/connections_graph.py measurements/database_perf/01_database_performance_large_consolidated_ "Large Database Performance X Number of Connections"
python python/connections_graph.py measurements/database_network/01_database_time_amazon_network_consolidated_ "Database Network Performance X Number of Connections (Small Message)" network_db
python python/connections_graph.py measurements/database_network/01_database_time_amazon_network_large_consolidated_ "Database Network Performance X Number of Connections (Large Message)" network_db
python python/connections_graph.py measurements/network/01_network_consolidated_ "Network Performance X Number of Connections" network
python python/system_trace_graph.py measurements/system_3_factor/01_2_mid_08_conn_norm_db_
python python/x_factor_graph.py measurements/msg_size_x_dataset/01_msg_size_x_dataset_consolidated_ "Message Size x Dataset" "msg_size_x_dataset"
python python/x_factor_graph.py measurements/system_3_factor/01_system_3_factor_consolidated_ "Middleware Count X Database Connections X Database Instance Size" "system_3_factor"
python python/per_request_type_database_graph.py measurements/database_perf/01_database_performance_consolidated_micro_benchmark_baseline.csv measurements/database_perf/01_database_performance_consolidated_micro_benchmark_large.csv
python python/client_number_graph.py measurements/system_client_number/01_system_client_number_consolidated_ "System Performance X Number of Clients"
python python/per_request_type_graph.py measurements/system_client_number/01_system_client_number_per_request_type_consolidated_response_time.csv "Request Type Response Time X Number of Clients"
python python/micro_benchmark_graph.py measurements/system_client_number/01_system_client_number_client_time_consolidated_response_time.csv measurements/system_client_number/01_system_client_number_middle_time_consolidated_response_time.csv "Response Time at Each Part of the System X Number of Clients"
python python/micro_benchmark_graph.py measurements/msg_size_x_dataset/01_system_msg_size_x_dataset_client_time_consolidated_response_time.csv measurements/msg_size_x_dataset/01_system_msg_size_x_dataset_middle_time_consolidated_response_time.csv "Response Time at Each Part of the System X Message Size and Dataset" no_x_title
python python/per_request_type_middle_graph.py measurements/msg_size_x_dataset/01_system_msg_size_x_dataset_per_request_type_consolidated_middle_response_time.csv "Database Time X Message Size and Dataset"
python python/micro_benchmark_graph.py measurements/system_3_factor/01_system_3_factor_consolidated_client_time_micro_benchmark.csv measurements/system_3_factor/01_system_3_factor_consolidated_middle_time_micro_benchmark.csv "Response Time at Each Part of the System X\nMiddleware Connections, Database Connections and Database Instance " no_x_title big
python python/system_trace_with_gc_graph.py measurements/system_3_factor/01_2_mid_08_conn_norm_db_