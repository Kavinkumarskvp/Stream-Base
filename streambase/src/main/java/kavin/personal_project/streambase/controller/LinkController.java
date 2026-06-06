package kavin.personal_project.streambase.controller;

import kavin.personal_project.streambase.dto.CreateLinkRequest;
import kavin.personal_project.streambase.dto.LinkDto;
import kavin.personal_project.streambase.service.LinkService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@AllArgsConstructor
@RequestMapping
public class LinkController {

    private final LinkService linkService;

    @PostMapping(path = "/api/links")
    public ResponseEntity<LinkDto> createLink(@RequestBody CreateLinkRequest request) {

        var link = linkService.createLink(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(link);
    }

    @GetMapping(path = "/s/{code}")
    public ResponseEntity<Void> redirect(@PathVariable("code") String code) {

        var url = linkService.redirect(code);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(url)).build();
    }
}
