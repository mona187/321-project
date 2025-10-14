import androidx.compose.runtime.Composable
import com.example.cpen_321.ui.viewmodels.MatchViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun WaitingRoomScreen(
    viewModel: MatchViewModel,
    onGroupReady: () -> Unit
){
    val state by viewModel.state.collectAsState()
    if (state.groupReady){
        onGroupReady()
    }
}