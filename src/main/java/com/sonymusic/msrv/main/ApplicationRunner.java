package com.sonymusic.msrv.main;

import com.sonymusic.msrv.service.S3ReporterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ApplicationRunner implements CommandLineRunner {
    @Autowired
    private S3ReporterService s3ReporterService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Fuck me up the ass");
        List l = s3ReporterService.getUnprocessedS3Files(Long.valueOf(59));
        System.out.println("Fuck me up the ass 2: " + l.size());
    }
}