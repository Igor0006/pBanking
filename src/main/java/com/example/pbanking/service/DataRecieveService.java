package com.example.pbanking.service;

import org.springframework.stereotype.Service;

import com.example.pbanking.config.BanksProperties;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DataRecieveService {
    private final BanksProperties banks;
}
