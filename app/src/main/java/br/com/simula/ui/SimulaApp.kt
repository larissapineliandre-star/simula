package br.com.simula.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.*
import androidx.room.*
import br.com.simula.data.*
import br.com.simula.notifications.ReminderScheduler
import br.com.simula.parser.PdfParser
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.*

class SimulaViewModel(private val app:Context):ViewModel(){ private val dao=SimulaDatabase.get(app).dao(); val concursos=dao.concursos().stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList()); var selecionado by mutableStateOf<Concurso?>(null); fun criar(nome:String,data:Long,onDone:()->Unit){viewModelScope.launch{val c=Concurso(nome=nome,dataProva=data); val id=dao.inserir(c); val saved=c.copy(id=id); ReminderScheduler.schedule(app,saved); selecionado=saved;onDone()}}; fun abrir(c:Concurso){selecionado=c}; fun questoes(id:Long)=dao.questoes(id).stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList()); fun provas(id:Long)=dao.provas(id).stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList()); fun adicionar(c:Concurso,p:Uri,g:Uri,pn:String,gn:String,onDone:()->Unit){viewModelScope.launch{val pid=dao.inserirProva(Prova(concursoId=c.id,nomeArquivoProva=pn,uriProva=p.toString(),nomeArquivoGabarito=gn,uriGabarito=g.toString())); val qs=PdfParser.questoes(app,p,c.id,pid); val map=PdfParser.gabarito(app,g); dao.inserirQuestoes(qs.map{it.copy(respostaCorreta=map[it.numero])});onDone()}}; fun responder(q:Questao,answer:String){viewModelScope.launch{val old=dao.questao(q.id); val d=dao.salvarDesempenho(DesempenhoQuestao(q.id,(old?.let{0}?:0)+1,0,0,answer,System.currentTimeMillis()))}} }
class VMFactory(private val c:Context):ViewModelProvider.Factory{override fun <T:ViewModel> create(x:Class<T>)=SimulaViewModel(c) as T}

@Composable fun SimulaApp(context:Context){val vm:SimulaViewModel=viewModel(factory=VMFactory(context)); var screen by remember{mutableStateOf("home")}; MaterialTheme{when(screen){"home"->Home(vm){screen="new"}{c->vm.abrir(c);screen="folder"};"new"->NewContest{n,d->vm.criar(n,d){screen="folder"}};"folder"->Folder(vm,vm.selecionado!!,context){screen="add"}{screen="questions"}{screen="quiz"};"add"->AddPair(vm,vm.selecionado!!){screen="folder"};"questions"->Questions(vm,vm.selecionado!!){screen="folder"};"quiz"->Quiz(vm,vm.selecionado!!){screen="folder"}}}}
@Composable fun Shell(title:String,back:(()->Unit)?=null,content:@Composable ColumnScope.()->Unit){Column(Modifier.fillMaxSize().padding(20.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(title,style=MaterialTheme.typography.headlineMedium);if(back!=null)TextButton(back){Text("Voltar")}}Spacer(Modifier.height(20.dp));content()}}
@Composable fun Home(vm:SimulaViewModel,new:()->Unit,open:(Concurso)->Unit)=Shell("Meus concursos"){Button(new){Text("+ Novo concurso")};Spacer(Modifier.height(16.dp));LazyColumn{items(vm.concursos.value){c->Card(Modifier.fillMaxWidth().padding(vertical=5.dp),onClick={open(c)}){Column(Modifier.padding(16.dp)){Text(c.nome);Text(DateFormat.getDateInstance().format(Date(c.dataProva)),color=MaterialTheme.colorScheme.secondary)}}}}}
@Composable fun NewContest(done:(String,Long)->Unit){var n by remember{mutableStateOf("")};var date by remember{mutableStateOf(Calendar.getInstance().apply{add(Calendar.MONTH,1)})};Shell("Novo concurso"){OutlinedTextField(n,{n=it},label={Text("Nome do concurso")},modifier=Modifier.fillMaxWidth());Spacer(Modifier.height(12.dp));Text("Data: ${DateFormat.getDateInstance().format(date.time)}");Button({date=Calendar.getInstance().apply{add(Calendar.DAY_OF_MONTH,1)}},{Text("Usar amanhã")});Spacer(Modifier.height(12.dp));Button({if(n.isNotBlank())done(n,date.timeInMillis)},{Text("Criar concurso")})}}
@Composable fun Folder(vm:SimulaViewModel,c:Concurso,context:Context,add:()->Unit,questions:()->Unit,quiz:()->Unit)=Shell(c.nome){Text("Prova: ${DateFormat.getDateInstance().format(Date(c.dataProva))}");Button(add){Text("Adicionar prova + gabarito")};Button(questions){Text("Banco de questões")};Button(quiz){Text("Gerar simulado")};Text("Os PDFs originais permanecem referenciados pelo armazenamento do Android.",style=MaterialTheme.typography.bodySmall)}
