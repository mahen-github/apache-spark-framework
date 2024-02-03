package dev.template.spark

import org.apache.hadoop.fs.Path
import org.apache.spark.SparkContext
import org.apache.spark.sql.SparkSession

//https://github.com/GoogleCloudPlatform/dataproc-scala-examples
//https://cloud.google.com/sdk/gcloud/reference/dataproc/jobs/submit/spark
object WordCount extends App {

  val spark: SparkSession = SparkSession
    .builder()
    .appName("Spark WordCount example")
    .enableHiveSupport()
    //    .config("spark.sql.warehouse.dir", "file:///tmp/spark-warehouse/tables")
    .config("fs.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem")
    .config("fs.AbstractFileSystem.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFS")
    .config("fs.gs.auth.service.account.enable", "true")
    .config("hive.metastore.warehouse.dir", "gs://<HIVE_WAREHOUSE_BUCKET>/hive-warehouse")
    .config("spark.sql.parquet.enableVectorizedReader", "false")
    .config("spark.sql.extensions", "io.delta.sql.DeltaSparkSessionExtension")
    .config("spark.sql.catalog.spark_catalog", "org.apache.spark.sql.delta.catalog.DeltaCatalog")
    .getOrCreate()

  private val FS_GS_PROJECT_ID = "fs.gs.project.id"

  private val FS_GS_SYSTEM_BUCKET = "fs.gs.system.bucket"

  private val FS_GS_WORKING_DIR = "fs.gs.working.dir"

  var sparkContext = spark.sparkContext

  def executeWordCount(sparkContext: SparkContext, args: Array[String]): Unit = {

    try {
      if (args.length != 2) {
        throw new IllegalArgumentException(
          "Exactly 2 arguments are required: <inputPath> <outputPath>"
        )
      }
      val inputPath = args(0)
      val outputPath = args(1)

      val conf = sparkContext.hadoopConfiguration
      conf.set("fs.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem")
      conf.set("fs.AbstractFileSystem.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFS")
      conf.set("fs.gs.auth.service.account.enable", "true")
      val gcsOutputPath = new Path(String.format(outputPath, conf.get("fs.gs.system.bucket")))
      val gcsFs = gcsOutputPath.getFileSystem(conf)

      if (gcsFs.exists(gcsOutputPath) && !gcsFs.delete(gcsOutputPath, true)) {
        System.err.println("Failed to delete the output directory: " + gcsOutputPath)
      } else {
        System.out.println("Delete the output bucket")
      }

      val lines = sparkContext.textFile(inputPath)
      val words = lines.flatMap(line => line.split(" "))
      val wordCounts = words.map(word => (word, 1)).reduceByKey(_ + _)
      wordCounts.saveAsTextFile(outputPath)
    } catch {
      case e: Throwable => throw new RuntimeException(e)
    }
  }

  executeWordCount(sparkContext, args)

}
