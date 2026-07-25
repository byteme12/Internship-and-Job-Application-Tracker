package com.usj.tracker.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String home() {
        return """
                <html>
                <head><title>Internship and Job Application Tracker API</title></head>
                <body style="font-family: sans-serif; max-width: 600px; margin: 40px auto;">
                    <h1>Internship and Job Application Tracker API</h1>
                    <p>The backend is running. This is a REST API, here are the available endpoints:</p>
                    <ul>
                        <li>GET /api/applications</li>
                        <li>GET /api/applications/{id}</li>
                        <li>POST /api/applications</li>
                        <li>GET /api/applications/{id}/next-states</li>
                        <li>PATCH /api/applications/{id}/transition</li>
                        <li>POST /api/applications/{id}/documents</li>
                        <li>DELETE /api/applications/{id}</li>
                        <li>GET /api/companies</li>
                        <li>GET /api/companies/{id}</li>
                        <li>POST /api/companies</li>
                    </ul>
                </body>
                </html>
                """;
    }
}
