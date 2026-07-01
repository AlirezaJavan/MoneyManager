package com.javanapps.moneymanager.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.javanapps.moneymanager.MainActivity
import com.javanapps.moneymanager.R
import com.javanapps.moneymanager.core.data.repository.TransactionRepository
import com.javanapps.moneymanager.core.model.MonthlySummary
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import io.github.alirezajavan.shamsipicker.calendar.ShamsiCalendar
import kotlinx.coroutines.flow.first
import kotlin.math.abs

class MonthSummaryWidget : GlanceAppWidget() {
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetEntryPoint {
        fun transactionRepository(): TransactionRepository
    }

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        val entryPoint =
            EntryPointAccessors.fromApplication(
                context.applicationContext,
                WidgetEntryPoint::class.java,
            )
        val currentMonth = ShamsiCalendar.now().monthKey
        val summary =
            entryPoint
                .transactionRepository()
                .observeMonthlySummary(currentMonth)
                .first()
        val monthName = ShamsiCalendar.MONTH_NAMES[currentMonth.month - 1]

        provideContent {
            GlanceTheme {
                WidgetContent(
                    monthName = "$monthName ${currentMonth.year}",
                    summary = summary,
                )
            }
        }
    }
}

@Composable
private fun WidgetContent(
    monthName: String,
    summary: MonthlySummary,
) {
    Box(
        modifier =
            GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.surface)
                .clickable(actionStartActivity<MainActivity>())
                .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = monthName,
                style =
                    TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = TEXT_SIZE_HEADER,
                        fontWeight = FontWeight.Medium,
                    ),
            )

            Spacer(modifier = GlanceModifier.height(SPACER_HEIGHT))

            Text(
                text = toman(summary.balanceToman),
                style =
                    TextStyle(
                        color = if (summary.balanceToman >= 0) GlanceTheme.colors.primary else GlanceTheme.colors.error,
                        fontSize = TEXT_SIZE_BALANCE,
                        fontWeight = FontWeight.Bold,
                    ),
            )
            Text(
                text = LocalContext.current.getString(R.string.widget_balance_label),
                style =
                    TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = TEXT_SIZE_LABEL,
                    ),
            )

            Spacer(modifier = GlanceModifier.height(SPACER_HEIGHT))

            Row(modifier = GlanceModifier.fillMaxWidth()) {
                Column(
                    modifier = GlanceModifier.defaultWeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = toman(summary.incomeToman),
                        style =
                            TextStyle(
                                color = GlanceTheme.colors.primary,
                                fontSize = TEXT_SIZE_HEADER,
                            ),
                    )
                    Text(
                        text = LocalContext.current.getString(R.string.widget_income_label),
                        style =
                            TextStyle(
                                color = GlanceTheme.colors.onSurfaceVariant,
                                fontSize = TEXT_SIZE_LABEL,
                            ),
                    )
                }
                Spacer(modifier = GlanceModifier.width(8.dp))
                Column(
                    modifier = GlanceModifier.defaultWeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = toman(summary.expenseToman),
                        style =
                            TextStyle(
                                color = GlanceTheme.colors.error,
                                fontSize = TEXT_SIZE_HEADER,
                            ),
                    )
                    Text(
                        text = LocalContext.current.getString(R.string.widget_expense_label),
                        style =
                            TextStyle(
                                color = GlanceTheme.colors.onSurfaceVariant,
                                fontSize = TEXT_SIZE_LABEL,
                            ),
                    )
                }
            }
        }
    }
}

private val TEXT_SIZE_HEADER = 13.sp
private val TEXT_SIZE_BALANCE = 22.sp
private val TEXT_SIZE_LABEL = 11.sp
private val SPACER_HEIGHT = 10.dp
private const val TOMAN_SUFFIX = " ت"

private fun toman(amount: Long): String {
    val abs = abs(amount)
    val digits = abs.toString()
    val grouped = StringBuilder()
    digits.reversed().forEachIndexed { i, c ->
        if (i > 0 && i % 3 == 0) grouped.append(',')
        grouped.append(c)
    }
    val formatted = grouped.reverse().toString()
    return if (amount < 0) "-$formatted$TOMAN_SUFFIX" else "$formatted$TOMAN_SUFFIX"
}

class MonthSummaryWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MonthSummaryWidget()
}
