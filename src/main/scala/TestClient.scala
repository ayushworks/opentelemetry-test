package ayushworks
package opentelemetry

import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.trace.TracerSdkProvider
import io.opentelemetry.sdk.trace.export.SimpleSpansProcessor
import io.opentelemetry.trace.{Span, Tracer}
import io.opentelemetry.exporters.logging.LoggingSpanExporter

object TestClient extends App {

  val tracerProvider: TracerSdkProvider = OpenTelemetrySdk.getTracerProvider
  val tracer: Tracer = tracerProvider.get("Trace Example")

  tracerProvider.addSpanProcessor(SimpleSpansProcessor.create(new LoggingSpanExporter))

  val multiAttrSpan: Span = tracer.spanBuilder("Span 1").startSpan
  multiAttrSpan.setAttribute("Attribute 1.1", "first attribute value")
  multiAttrSpan.setAttribute("Attribute 1.2", "second attribute value")
  multiAttrSpan.end


  val multiAttrSpan1: Span = tracer.spanBuilder("Span 2").setParent(multiAttrSpan).startSpan
  multiAttrSpan1.setAttribute("Attribute 2.1", "first attribute value")
  multiAttrSpan1.setAttribute("Attribute 2.2", "second attribute value")
  multiAttrSpan1.end

  OpenTelemetrySdk.getTracerProvider.shutdown

  private def printTraceConfig(): Unit = {
    val config = tracerProvider.getActiveTraceConfig
    System.err.println("==================================")
    System.err.print("Max number of attributes: ")
    System.err.println(config.getMaxNumberOfAttributes)
    System.err.print("Max number of attributes per event: ")
    System.err.println(config.getMaxNumberOfAttributesPerEvent)
    System.err.print("Max number of attributes per link: ")
    System.err.println(config.getMaxNumberOfAttributesPerLink)
    System.err.print("Max number of events: ")
    System.err.println(config.getMaxNumberOfEvents)
    System.err.print("Max number of links: ")
    System.err.println(config.getMaxNumberOfLinks)
    System.err.print("Sampler: ")
    System.err.println(config.getSampler.getDescription)
  }
}
