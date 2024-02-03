package dev.template.spark

import org.apache.log4j.Level
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.SparkSession

trait SparkSessionWrapper extends Logger with AutoCloseable {

  lazy val spark = SparkSession
    .builder()
    .appName("Spark CovidDataPartitioner example")
    .enableHiveSupport()
    .config("fs.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem")
    .config("fs.AbstractFileSystem.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFS")
    .config("fs.gs.auth.service.account.enable", "true")
    .config("hive.metastore.warehouse.dir", "gs://<HIVE_WAREHOUSE_BUCKET>/hive-warehouse")
    .config("spark.sql.warehouse.dir", "gs://<HIVE_WAREHOUSE_BUCKET>/spark-warehouse")
    // For local testing
    //    .config("spark.hadoop.google.cloud.auth.service.account.json.keyfile", "/path/to/key/keyfile.json")
    .config("spark.sql.parquet.enableVectorizedReader", "false")
    // hive
    .config("spark.hadoop.hive.exec.dynamic.partition", "true")
    .config("spark.hadoop.hive.exec.dynamic.partition.mode", "nonstrict")
    .getOrCreate()

  val sc: SparkContext = spark.sparkContext
  sc.setLogLevel(Level.INFO.toString)
  val sqlContext: SQLContext = spark.sqlContext

  override def close(): Unit = spark.close()
}
