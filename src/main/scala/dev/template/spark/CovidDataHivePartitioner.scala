package dev.template.spark

import dev.template.spark.sink.Writer
import dev.template.spark.source.Reader
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.Row
import org.apache.spark.sql.SaveMode
import org.apache.spark.sql.SparkSession

object CovidDataHivePartitioner
    extends App
    with SparkSessionWrapper
    with Reader
    with Writer
    with Logger {

  def writeParquet(
    spark: SparkSession,
    file: String,
    outputPath: String,
    schema_location: String
  ): Unit = {
    val covidData: Dataset[Row] = readCsv(spark).csv(file).repartition(1)
    log.info(covidData.printSchema())
    covidData.createOrReplaceTempView("covid")

    val groupedView = spark.sqlContext
      .sql("""
             | select
             |  cast(to_date(date, "yyyy-MM-dd") as String) as reported_date,
             |  county,
             |  state,
             |  fips,
             |  cases,
             |  deaths from covid
             |
             |""".stripMargin)
      .cache()

    val createSchema = spark.sql(s""" create schema if not exists public_data
                                    |LOCATION "$schema_location"
                                    |""".stripMargin)
    createSchema.show()

    log.info(s"Schema Created ${createSchema.printSchema()}")

    val tableCreated = spark.sql(s""" create external table if not exists public_data.covid
                                    | (
                                    | reported_date date,
                                    | county string,
                                    | state string,
                                    | fips int,
                                    | cases int,
                                    | deaths int
                                    |)
                                    |  partitioned by (reported_date)
                                    |  Stored as parquet
                                    |LOCATION "$outputPath"
                                    |""".stripMargin)

    log.info(s"Table created $tableCreated.printSchema() ")

    writeParquetAsTable(
      groupedView,
      SaveMode.Overwrite,
      "poc.covid",
      path = outputPath,
      Some("reported_date")
    )
  }

  if (args.length == 0) {
    println(""" USAGE :
              | spark-submit \
              | --class dev.template.spark.CovidDataPartitioner \
              | --packages io.delta:delta-core_2.12:2.4.0 \
              | --master spark://localhost:7077 \
              | --deploy-mode client \
              | --driver-memory 1g \
              | --executor-memory 1g \
              | --executor-cores 2 \
              | build/libs/spark-scala-gradle-bootstrap-2.12.0-all.jar \
              | src/main/resources/us-counties-recent.csv \
              | /tmp/partitioned-covid-data
              |""".stripMargin)
    throw new RuntimeException("Requires input file us-counties-recent.csv")
  }

  val inputFilePath = args(0)
  val outputPath: String = args(1)
  val hive_schema_path: String = args(2)
  log.info("Input path " + inputFilePath)
  log.info("Output path " + outputPath)

  writeParquet(spark, inputFilePath, outputPath, hive_schema_path)

}
