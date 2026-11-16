package com.nb.app.presentation.home

import com.nb.app.domain.model.Team
import nbapp.shared.generated.resources.Res
import nbapp.shared.generated.resources.sort_city
import nbapp.shared.generated.resources.sort_conference
import nbapp.shared.generated.resources.sort_name
import org.jetbrains.compose.resources.StringResource

/**
 * Orders available in the Home sort dialog. Ties are broken by the team's full name
 * so the order is always stable.
 */
enum class TeamSortOption(
    val label: StringResource,
    val comparator: Comparator<Team>,
) {
    NAME(
        label = Res.string.sort_name,
        comparator = compareBy { it.fullName },
    ),
    CITY(
        label = Res.string.sort_city,
        comparator = compareBy<Team> { it.city }.thenBy { it.fullName },
    ),
    CONFERENCE(
        label = Res.string.sort_conference,
        comparator = compareBy<Team> { it.conference }.thenBy { it.fullName },
    ),
}
