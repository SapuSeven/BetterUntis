
import androidx.compose.runtime.Composable
import com.sapuseven.untis.persistence.entity.ElementEntity

@Composable
fun ElementItem(
	element: ElementEntity,
	content: @Composable (shortName: String, longName: String, isAllowed: Boolean) -> Unit
) {
	content(element.getShortName(), element.getLongName(), element.isAllowed())
}
