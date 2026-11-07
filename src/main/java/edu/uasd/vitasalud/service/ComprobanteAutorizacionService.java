package edu.uasd.vitasalud.service;
import edu.uasd.vitasalud.model.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
/**
 * RF-AUT-11 (P2): PDF textual de una página, sin bibliotecas adicionales.
 * Emplea Helvetica y estructura PDF 1.4 mínima. Para PDFs complejos usar una librería aprobada.
 * Nunca incluir datos reales de pacientes en la demo ni compartir comprobantes fuera del entorno.
 */
public class ComprobanteAutorizacionService {
 public void generar(Autorizacion a,List<DetalleAutorizacion> detalles,Path destino)throws IOException{
    if(a.estado()!=EstadoAutorizacion.APROBADA)
        throw new ServicioException("Solo se emiten comprobantes APROBADOS");
    List<String> renglones=new ArrayList<>();
    renglones.add("ARS VitaSalud - Comprobante de autorizacion");
    renglones.add("Autorizacion: "+a.numero()+"  Estado: "+a.estado());
    renglones.add("Afiliado: "+a.afiliadoNombre());
    renglones.add("Prestador: "+a.prestadorNombre());
    renglones.add("Servicio: "+a.tipoServicio());
    renglones.add("Diagnostico: "+a.diagnosticoCie10());
    renglones.add("Monto solicitado: RD$ "+a.montoSolicitado());
    renglones.add("Monto cubierto: RD$ "+a.montoCubierto());
    renglones.add("Copago: RD$ "+a.copago());
    renglones.add("Vence: "+a.fechaVencimiento());
    renglones.add("PROCEDIMIENTOS:");
    for(DetalleAutorizacion d:detalles)renglones.add(d.codigo()+" x"+d.cantidad()+" · RD$ "+d.montoCubierto());
    StringBuilder contenido=new StringBuilder("BT /F1 11 Tf 48 785 Td 15 TL\n");
    for(String linea:renglones){
        contenido.append("(").append(escapePdf(linea)).append(") Tj T*\n");
    }
    contenido.append("ET\n");
    byte[] stream=contenido.toString().getBytes(StandardCharsets.ISO_8859_1);
    List<byte[]> obj=new ArrayList<>();
    obj.add(b("<< /Type /Catalog /Pages 2 0 R >>"));
    obj.add(b("<< /Type /Pages /Kids [3 0 R] /Count 1 >>"));
    obj.add(b("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>"));
    obj.add(b("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>"));
    byte[] prefijo=b("<< /Length "+stream.length+" >>\nstream\n");
    byte[] fin=b("endstream");
    ByteArrayOutputStream ultimo=new ByteArrayOutputStream();ultimo.write(prefijo);ultimo.write(stream);ultimo.write(fin);
    obj.add(ultimo.toByteArray());
    ByteArrayOutputStream pdf=new ByteArrayOutputStream();pdf.write(b("%PDF-1.4\n"));
    List<Integer> desplazamientos=new ArrayList<>();desplazamientos.add(0);
    for(int i=0;i<obj.size();i++){
        desplazamientos.add(pdf.size());pdf.write(b((i+1)+" 0 obj\n"));pdf.write(obj.get(i));pdf.write(b("\nendobj\n"));
    }
    int inicio=pdf.size();pdf.write(b("xref\n0 "+(obj.size()+1)+"\n0000000000 65535 f \n"));
    for(int i=1;i<desplazamientos.size();i++)pdf.write(b(String.format("%010d 00000 n \n",desplazamientos.get(i))));
    pdf.write(b("trailer\n<< /Size "+(obj.size()+1)+" /Root 1 0 R >>\nstartxref\n"+inicio+"\n%%EOF"));
    Files.write(destino,pdf.toByteArray());
 }
 private static byte[] b(String s){return s.getBytes(StandardCharsets.ISO_8859_1);}
 private static String escapePdf(String s){
    if(s==null)return "";
    // PDF básico con Helvetica Latin-1; se sustituyen caracteres no representables.
    StringBuilder r=new StringBuilder();
    for(char ch:s.toCharArray()){
        char c=(ch>=32&&ch<=255)?ch:' ';
        if(c=='('||c==')'||c=='\\')r.append('\\');
        r.append(c);
    }
    return r.toString();
 }
}
