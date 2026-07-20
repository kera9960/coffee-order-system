package com.example.coffeeordersystem.domain.menu.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.coffeeordersystem.domain.menu.entity.Menu;
import com.example.coffeeordersystem.domain.menu.entity.MenuStatus;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    Page<Menu> findAllByStatus(MenuStatus status, Pageable pageable);

    List<Menu> findAllByIdIn(Collection<Long> ids);
}
