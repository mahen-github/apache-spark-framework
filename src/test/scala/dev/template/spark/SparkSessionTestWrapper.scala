package dev.template.spark

import org.apache.spark.sql.SparkSession

trait SparkSessionTestWrapper {

  val spark = SparkSession.builder
    .master("local[*]")
    .config("spark.hadoop.hive.exec.dynamic.partition.mode", "nostrict")
    .config("spark.sql.warehouse.dir", "file:///tmp/poc/spark-warehouse")
    .config("spark.sql.parquet.enableVectorizedReader", "false")
    .config("hive.exec.dynamic.partition", "true")
    .config("spark.sql.catalog.spark_catalog", "org.apache.spark.sql.delta.catalog.DeltaCatalog")
    .enableHiveSupport()
    .getOrCreate()

}
