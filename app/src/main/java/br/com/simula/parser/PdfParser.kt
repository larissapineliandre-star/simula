package br.com.simula.parser

import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import br.com.simula.data.Questao
import java.util.regex.Pattern

object PdfParser {
 fun text(context: Context, uri: Uri): String { PDFBoxResourceLoader.init(context); context.contentResolver.openInputStream(uri).use { input -> requireNotNull(input); PDDocument.load(input).use { return PDFTextStripper().getText(it) } } }
 fun questoes(context: Context, uri: Uri, concursoId: Long, provaId: Long): List<Questao> {
  val raw=text(context,uri).replace("\r","\n"); val starts=Pattern.compile("(?m)^(?:Quest(?:ão|ao)\\s*)?(\\d{1,3})[.) -]+(.*)$").matcher(raw); val out=mutableListOf<Questao>(); val found=mutableListOf<Pair<Int,Int>>(); while(starts.find()) found+=starts.group(1)!!.toInt() to starts.start()
  found.forEachIndexed { i,(num,pos)-> val end=if(i+1<found.size)found[i+1].second else raw.length; val block=raw.substring(pos,end).trim(); val lines=block.lines(); val split=Regex("(?m)^[ ]*([A-Ea-e])[.)\\-:]\\s*").findAll(block).toList(); val first=split.firstOrNull()?.range?.first ?: block.length; val statement=block.substringAfterFirstNumber().substring(0, first.coerceAtMost(block.substringAfterFirstNumber().length)).trim(); fun alt(letter:String)=split.firstOrNull{it.groupValues[1].equals(letter,true)}?.let{block.substring(it.range.last+1, split.dropWhile{ x->x!=it}.drop(1).firstOrNull()?.range?.first ?: block.length).trim()} ; out+=Questao(concursoId=concursoId,provaId=provaId,numero=num,enunciadoOriginal=statement,alternativaA=alt("A"),alternativaB=alt("B"),alternativaC=alt("C"),alternativaD=alt("D"),alternativaE=alt("E"),respostaCorreta=null) }
  return out
 }
 private fun String.substringAfterFirstNumber()=replace(Regex("^(?:Quest(?:ão|ao)\\s*)?\\d{1,3}[.) -]+"),"").trim()
 fun gabarito(context: Context, uri: Uri): Map<Int,String> = Regex("(?m)(\\d{1,3})\\s*(?:[-.) :]\\s*)?([A-Ea-e])\\b").findAll(text(context,uri)).associate{it.groupValues[1].toInt() to it.groupValues[2].uppercase()}
}
