package br.com.simula.ui.theme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
private val Blue=Color(0xFF8FD3FF)
private val Dark=darkColorScheme(primary=Blue,onPrimary=Color.Black,background=Color.Black,surface=Color(0xFF111111),onBackground=Color.White,onSurface=Color.White,secondary=Blue)
@Composable fun SimulaTheme(content:@Composable()->Unit){MaterialTheme(colorScheme=Dark,typography=Typography().let{it.copy(titleLarge=it.titleLarge.copy(fontFamily=androidx.compose.ui.text.font.FontFamily.Serif),bodyLarge=it.bodyLarge.copy(fontFamily=androidx.compose.ui.text.font.FontFamily.Serif))},content=content)}
