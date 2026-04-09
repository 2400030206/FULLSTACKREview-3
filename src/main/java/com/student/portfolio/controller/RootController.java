package com.student.portfolio.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> root() {
        String html = """
                <!doctype html>
                <html lang=\"en\">
                <head>
                    <meta charset=\"UTF-8\" />
                    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />
                    <title>Student Portfolio Backend</title>
                    <style>
                        body { font-family: Arial, sans-serif; margin: 40px; line-height: 1.5; }
                        h1 { color: #1f4e79; }
                        .card { max-width: 680px; padding: 20px; border: 1px solid #ddd; border-radius: 8px; }
                        a { color: #0b5ed7; text-decoration: none; }
                        a:hover { text-decoration: underline; }
                    </style>
                </head>
                <body>
                    <div class=\"card\">
                        <h1>Student Portfolio Backend Running</h1>
                        <p>This backend is running successfully.</p>
                        <p>API Home: <a href=\"/api/\">/api/</a></p>
                    </div>
                </body>
                </html>
                """;
        return ResponseEntity.ok(html);
    }
}