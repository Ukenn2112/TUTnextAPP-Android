package com.meikenn.tama.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.meikenn.tama.domain.model.Assignment
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun AssignmentCard(
    assignment: Assignment,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeColor = when {
        assignment.isOverdue -> Color(0xFFD32F2F)
        assignment.isUrgent -> Color(0xFFE65100)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val timeBgColor = when {
        assignment.isOverdue -> Color(0xFFD32F2F).copy(alpha = 0.1f)
        assignment.isUrgent -> Color(0xFFE65100).copy(alpha = 0.1f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top row: course name + remaining time badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = assignment.courseName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = timeBgColor
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = timeColor,
                            modifier = Modifier.padding(0.dp)
                        )
                        Text(
                            text = assignment.remainingTimeText,
                            style = MaterialTheme.typography.labelSmall,
                            color = timeColor
                        )
                    }
                }
            }

            // Title
            Text(
                text = assignment.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Description
            if (assignment.description.isNotBlank()) {
                Text(
                    text = assignment.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Bottom row: due date + chevron
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${formatDate(assignment)} 締切",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatDate(assignment: Assignment): String {
    val formatter = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.JAPAN)
    return formatter.format(assignment.dueDate)
}
