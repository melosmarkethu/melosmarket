package com.melosmarket.api;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/page-views")
public class PageViewController {
    private final JdbcTemplate database;

    public PageViewController(JdbcTemplate database) {
        this.database = database;
    }

    @PostMapping
    public Map<String, Object> recordView() {
        LocalDate today = LocalDate.now(ZoneId.of("Europe/Budapest"));
        Long count = database.queryForObject("""
                insert into daily_page_views (view_date, view_count) values (?, 1)
                on conflict (view_date) do update
                set view_count = daily_page_views.view_count + 1
                returning view_count
                """, Long.class, today);
        return Map.of("date", today, "count", count);
    }

    @GetMapping
    public Map<String, Object> getToday() {
        LocalDate today = LocalDate.now(ZoneId.of("Europe/Budapest"));
        Long count = database.queryForObject("""
                select coalesce((select view_count from daily_page_views where view_date = ?), 0)
                """, Long.class, today);
        return Map.of("date", today, "count", count);
    }
}
