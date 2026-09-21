package br.com.simula

import android.app.*
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.simula.ui.SimulaApp
import br.com.simula.ui.theme.SimulaTheme

class MainActivity: ComponentActivity(){ private val permission=registerForActivityResult(ActivityResultContracts.RequestPermission()){}; override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState); if(android.os.Build.VERSION.SDK_INT>=33 && checkSelfPermission("android.permission.POST_NOTIFICATIONS")!=PackageManager.PERMISSION_GRANTED) permission.launch("android.permission.POST_NOTIFICATIONS"); setContent{SimulaTheme{SimulaApp(applicationContext)}}} }
