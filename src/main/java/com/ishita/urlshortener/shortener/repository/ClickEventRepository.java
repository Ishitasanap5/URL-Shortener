package com.ishita.urlshortener.shortener.repository;

import com.ishita.urlshortener.shortener.model.ClickEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClickEventRepository extends JpaRepository<ClickEvent,Long> {
}
