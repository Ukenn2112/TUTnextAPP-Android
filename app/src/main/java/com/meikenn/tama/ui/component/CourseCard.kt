package com.meikenn.tama.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meikenn.tama.domain.model.Course
import com.meikenn.tama.ui.theme.CourseColors

@Composable
fun CourseCard(
    course: Course,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val backgroundColor = CourseColors.getColor(course.colorIndex)
    val isLight = backgroundColor == Color.White

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = course.name,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 12.sp
                ),
                color = if (isLight) MaterialTheme.colorScheme.onSurface
                else Color(0xFF333333),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Start
            )

            if (course.room.isNotBlank()) {
                Text(
                    text = course.room,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        lineHeight = 11.sp
                    ),
                    color = if (isLight) MaterialTheme.colorScheme.onSurfaceVariant
                    else Color(0xFF555555),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Unread badge
        val unread = course.keijiMidokCnt ?: 0
        if (unread > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(14.dp)
                    .background(Color.Red, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (unread > 9) "9+" else unread.toString(),
                    color = Color.White,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 8.sp
                )
            }
        }
    }
}
