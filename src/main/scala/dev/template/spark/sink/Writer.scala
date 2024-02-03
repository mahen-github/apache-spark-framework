package dev.template.spark.sink

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.SaveMode

trait Writer {

  def writeParquet(
    df: DataFrame,
    path: String,
    mode: SaveMode = SaveMode.Overwrite,
    partition: Option[String]
  ): Unit = {
    val dfWriter = df.write
      .mode(mode)
    if (partition.isDefined) {
      dfWriter.partitionBy(partition.get)
    }
    dfWriter.save(path)
  }

  def writeParquetAsTable(
    df: DataFrame,
    mode: SaveMode = SaveMode.Overwrite,
    table: String,
    path: String,
    partition: Option[String]
  ): Unit = {
    val dfWriter = df.write.format("parquet").mode(mode)
    if (partition.isDefined) {
      dfWriter.partitionBy(partition.get)
    }
    dfWriter.options(Map("path" -> path)).saveAsTable(table)
  }

  def writeCsv(
    df: DataFrame,
    path: String,
    mode: SaveMode = SaveMode.Overwrite,
    options: Map[String, String] = Map("delimiter" -> ","),
    partition: Option[String] = None
  ): Unit = {
    val dfWriter = df.write
      .format("csv")
      .options(options)
      .mode(mode)
    if (partition.isDefined) {
      dfWriter.partitionBy(partition.get)
    }
    dfWriter.csv(path)
  }

}
