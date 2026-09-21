package br.com.simula.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "concursos") data class Concurso(@PrimaryKey(autoGenerate = true) val id: Long = 0, val nome: String, val dataProva: Long, val dataCriacao: Long = System.currentTimeMillis())
@Entity(tableName = "provas") data class Prova(@PrimaryKey(autoGenerate = true) val id: Long = 0, val concursoId: Long, val nomeArquivoProva: String, val uriProva: String, val nomeArquivoGabarito: String, val uriGabarito: String, val dataProcessamento: Long = System.currentTimeMillis())
@Entity(tableName = "questoes") data class Questao(@PrimaryKey(autoGenerate = true) val id: Long = 0, val concursoId: Long, val provaId: Long, val numero: Int, val enunciadoOriginal: String, val alternativaA: String?, val alternativaB: String?, val alternativaC: String?, val alternativaD: String?, val alternativaE: String?, val respostaCorreta: String?)
@Entity(tableName = "desempenho") data class DesempenhoQuestao(@PrimaryKey val questaoId: Long, val vezesRespondida: Int = 0, val vezesAcertada: Int = 0, val vezesErrada: Int = 0, val ultimaResposta: String? = null, val dataUltimaResposta: Long? = null)

@Dao interface SimulaDao {
 @Query("SELECT * FROM concursos ORDER BY dataProva") fun concursos(): Flow<List<Concurso>>
 @Insert suspend fun inserir(c: Concurso): Long
 @Update suspend fun atualizar(c: Concurso)
 @Query("SELECT * FROM concursos WHERE id=:id") suspend fun concurso(id: Long): Concurso?
 @Insert suspend fun inserirProva(p: Prova): Long
 @Insert suspend fun inserirQuestoes(q: List<Questao>)
 @Query("SELECT * FROM provas WHERE concursoId=:id ORDER BY dataProcessamento DESC") fun provas(id: Long): Flow<List<Prova>>
 @Query("SELECT * FROM questoes WHERE concursoId=:id ORDER BY numero") fun questoes(id: Long): Flow<List<Questao>>
 @Query("SELECT * FROM questoes WHERE id=:id") suspend fun questao(id: Long): Questao?
 @Query("SELECT * FROM desempenho WHERE questaoId IN (SELECT id FROM questoes WHERE concursoId=:id) AND vezesErrada > 0") fun erradas(id: Long): Flow<List<DesempenhoQuestao>>
 @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun salvarDesempenho(d: DesempenhoQuestao)
}
@Database(entities=[Concurso::class,Prova::class,Questao::class,DesempenhoQuestao::class], version=1, exportSchema=false)
abstract class SimulaDatabase: RoomDatabase(){ abstract fun dao(): SimulaDao; companion object { @Volatile private var instance: SimulaDatabase?=null; fun get(context: android.content.Context)=instance ?: synchronized(this){ instance ?: Room.databaseBuilder(context,SimulaDatabase::class.java,"simula.db").build().also{instance=it} } } }
