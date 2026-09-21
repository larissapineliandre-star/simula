package br.com.simula.notifications

import android.app.*
import android.content.*
import android.os.Build
import br.com.simula.data.Concurso
import java.text.DateFormat
import java.util.*

object ReminderScheduler {
 private const val channel="provas"
 fun schedule(context: Context, c: Concurso){ val nm=context.getSystemService(NotificationManager::class.java); if(Build.VERSION.SDK_INT>=26) nm.createNotificationChannel(NotificationChannel(channel,"Provas",NotificationManager.IMPORTANCE_DEFAULT)); val offsets=listOf(30L,7L,1L); offsets.forEachIndexed{idx,days-> val at=c.dataProva-days*86400000L; if(at>System.currentTimeMillis()){ val intent=PendingIntent.getBroadcast(context,(c.id*10+idx).toInt(),Intent(context,ReminderReceiver::class.java).putExtra("nome",c.nome).putExtra("data",c.dataProva),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE); context.getSystemService(AlarmManager::class.java).setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,at,intent) } } }
 fun cancel(context: Context,c:Concurso){ val a=context.getSystemService(AlarmManager::class.java); repeat(3){val p=PendingIntent.getBroadcast(context,(c.id*10+it).toInt(),Intent(context,ReminderReceiver::class.java),PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE); if(p!=null)a.cancel(p)} }
}
class ReminderReceiver: BroadcastReceiver(){ override fun onReceive(context:Context,intent:Intent){ val date=DateFormat.getDateInstance().format(Date(intent.getLongExtra("data",0))); val n=Notification.Builder(context,"provas").setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("Simula — Sua prova está chegando").setContentText("${intent.getStringExtra("nome")} • $date").setAutoCancel(true).build(); context.getSystemService(NotificationManager::class.java).notify(intent.hashCode(),n) } }
