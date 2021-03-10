package com.juanma.emprenglobal.controller;

import com.juanma.emprenglobal.model.*;
import org.checkerframework.checker.nullness.Opt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController()
@RequestMapping("/emprenglobal")
public class EmprenglobalController {

    @Autowired
    UserRepository repository;
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    UploadFileService uploadFileService;
    @Autowired
    NewsRepository newsRepository;
    @Autowired
    EventRepository eventRepository;
    @Autowired
    GalleryRepository galleryRepository;
    @Autowired
    PollRepository pollRepository;
    @Autowired
    OptionRepository optionRepository;
    @Autowired
    AuthorityRepo authorityRepo;

    /********USER********/
    @GetMapping("/user/{uid:\\d+}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public Optional<User> getUser(@PathVariable("uid") long uid) {
        return repository.findById(uid);
    }

    @GetMapping("/user/{uid:\\d+}/comments")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public List<Comment> getUserComments(@PathVariable("uid") long uid) {
        List<Comment> comments = new ArrayList<>();
        Optional<User> user = repository.findById(uid);
        user.ifPresent((u)-> comments.addAll( u.getComments()));
        return comments;
    }

    @GetMapping("/users/{uid:[\\d]+}/{img_name}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Resource> getUserImages(@PathVariable("uid") long uid,
                                                   @PathVariable("img_name") String imageName) {
        Path path = Paths.get(String.format(uploadFileService.getPathFormat(PicEntities.Users),uid, imageName));
        System.out.println(path);
        HttpHeaders header = new HttpHeaders();
        header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + path.getFileName());
        header.add("Cache-Control", "no-cache, no-store, must-revalidate");
        header.add("Pragma", "no-cache");
        header.add("Expires", "0");

        FileSystemResource resource = new FileSystemResource(path);
        return ResponseEntity.ok()
                .headers(header)
                .contentLength(path.toFile().length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @GetMapping("/user/{uname:[^\\d]+}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public Optional<User> getUserByName(@PathVariable("uname") String uname) {
        return repository.findByUsername(uname);
    }

    @PostMapping("/user")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<Object> putUser(@RequestParam("uid") long uid,
                          @RequestParam("uname") String uname,
                          @RequestParam("password") String password,
                          @RequestParam("email") String email,
                          @RequestParam("file") MultipartFile file,
                          @RequestParam("authorities") String authorities){
        User user = new User();
        user.setId(uid);
        user.setUsername(uname);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setPhotoPath(uploadFileService.uploadPhoto(uid, file, PicEntities.Users));
        Optional<User> optUser = repository.findById(uid);
        user.setAuthorities(boundAuthorities(authorities));
        repository.save(user);
        return new ResponseEntity<>(
                optUser.isPresent() ? "USER UPDATED" : "USER CREATED",
                HttpStatus.OK);
    }

    @DeleteMapping("/user/{uid}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public String deleteUser(@PathVariable("uid") long uid) {
        if (repository.existsById(uid)) {
            repository.deleteById(uid);
            return "USER DELETED";
        }
        return "INVALID USER ID";
    }

    @PutMapping("/user/{uid:\\d+}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<Object> updateUserPhoto(@PathVariable("uid") long uid,
                                                  @RequestPart(value="file") final MultipartFile file) {

        User user = repository.findById(uid).orElseThrow(InvalidParameterException::new);
        user.setPhotoPath(uploadFileService.uploadPhoto(uid, file, PicEntities.Users));
        repository.save(user);
        return new ResponseEntity<>(
                "USER UPDATED",
                HttpStatus.OK);
    }

    @PutMapping("/user")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<Object> uploadUserPass(@RequestBody()  Map<String, Object> payload) {
        long uid = (Integer) payload.get("id");
        User user  = repository.findById(uid)
                    .orElseThrow(InvalidParameterException::new);
        user.setPassword(passwordEncoder.encode((String) payload.get("password")));
        repository.save(user);
        return new ResponseEntity<>(
                "USER UPDATED",
                HttpStatus.OK);
    }


    /********COMMENT********/
    @GetMapping("/comment/{id:\\d+}")
    @PreAuthorize("permitAll()")
    public Optional<Comment> getComment(@PathVariable("id") long id) {
        return commentRepository.findById(id);
    }

    @GetMapping("/comments")
    @PreAuthorize("permitAll()")
    public List<Comment> getComment() {
        return commentRepository.findAll();
    }

    @PostMapping("/comment")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<Object> putComment(@RequestBody Map<String, Object> payload) {
        Comment comment = new Comment();
        comment.setText((String) payload.get("text"));
        long uid = (Integer) payload.get("uid");
        comment.setUser(repository.findById(uid).orElseThrow(InvalidParameterException::new));
        commentRepository.save(comment);

        return new ResponseEntity<>(
                "COMMENT CREATED",
                HttpStatus.OK);
    }

    @DeleteMapping("/comment/{id}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public String deleteComment(@PathVariable("id") long id) {
        if (commentRepository.existsById(id)) {
            commentRepository.deleteById(id);
            return "COMMENT DELETED";
        }
        return "INVALID COMMENT ID";
    }

    /********NEWS********/
    @GetMapping("/news/{id:\\d+}")
    @PreAuthorize("permitAll()")
    public Optional<News> getNews(@PathVariable("id") long id) {
        return newsRepository.findById(id);
    }

    @GetMapping("/news")
    @PreAuthorize("permitAll()")
    public List<News> getNewsList() {
        return newsRepository.findAll();
    }

    @PostMapping("/news")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<Object> putComment(@RequestPart("header") String header,
                                             @RequestPart("date") String date,
                                             @RequestPart("text") String text,
                                             @RequestPart("file") MultipartFile file) {
        News news = new News();
        news.setHeader(header);
        news.setDate(date);
        news.setText(text);
        news.setPhotoPath("/fake/path");
        news = newsRepository.save(news);

        news.setPhotoPath(uploadFileService.uploadPhoto(news.getId(), file, PicEntities.News));
        newsRepository.save(news);

        return new ResponseEntity<>(
                "NEWS CREATED",
                HttpStatus.OK);
    }


    @DeleteMapping("/news/{id}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public String deleteNews(@PathVariable("id") long id) {
        if (newsRepository.existsById(id)) {
            newsRepository.deleteById(id);
            return "NEWS DELETED";
        }
        return "INVALID NEWS ID";
    }

    @GetMapping("/news/{id:[\\d]+}/{img_name}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Resource> getNewsImage(@PathVariable("id") long id,
                                                 @PathVariable("img_name") String imageName) {
        Path path = Paths.get(String.format(uploadFileService.getPathFormat(PicEntities.News),id, imageName));

        HttpHeaders header = new HttpHeaders();
        header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + path.getFileName());
        header.add("Cache-Control", "no-cache, no-store, must-revalidate");
        header.add("Pragma", "no-cache");
        header.add("Expires", "0");

        FileSystemResource resource = new FileSystemResource(path);
        return ResponseEntity.ok()
                .headers(header)
                .contentLength(path.toFile().length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    /********EVENTS********/
    @GetMapping("/event/{id:\\d+}")
    @PreAuthorize("permitAll()")
    public Optional<Event> getEvent(@PathVariable("id") long id) {
        return eventRepository.findById(id);
    }

    @GetMapping("/events")
    @PreAuthorize("permitAll()")
    public List<Event> getEvents() {
        return eventRepository.findAll();
    }

    @PostMapping("/event")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<Object> putEvent(@RequestPart("header") String header,
                                           @RequestPart("date") String date,
                                           @RequestPart("location") String location,
                                           @RequestPart("text") String text,
                                           @RequestPart("file") MultipartFile file) {
        Event event = new Event();
        event.setHeader(header);
        event.setDate(date);
        event.setLocation(location);
        event.setText(text);
        event.setPhotoPath("/fake/path");
        event = eventRepository.save(event);

        event.setPhotoPath(uploadFileService.uploadPhoto(event.getId(), file, PicEntities.Events));
        eventRepository.save(event);

        return new ResponseEntity<>(
                "EVENT CREATED",
                HttpStatus.OK);
    }

    @DeleteMapping("/event/{id}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public String deleteEvent(@PathVariable("id") long id) {
        if (eventRepository.existsById(id)) {
            eventRepository.deleteById(id);
            return "EVENT DELETED";
        }
        return "INVALID EVENT ID";
    }

    @GetMapping("/events/{id:[\\d]+}/{img_name}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Resource> getEventImage(@PathVariable("id") long id,
                                                 @PathVariable("img_name") String imageName) {
        Path path = Paths.get(String.format(uploadFileService.getPathFormat(PicEntities.Events),id, imageName));

        HttpHeaders header = new HttpHeaders();
        header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + path.getFileName());
        header.add("Cache-Control", "no-cache, no-store, must-revalidate");
        header.add("Pragma", "no-cache");
        header.add("Expires", "0");

        FileSystemResource resource = new FileSystemResource(path);
        return ResponseEntity.ok()
                .headers(header)
                .contentLength(path.toFile().length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    /********GALLERY********/
    @GetMapping("/gallery/{id:\\d+}")
    @PreAuthorize("permitAll()")
    public Optional<Gallery> getGallery(@PathVariable("id") long id) {
        return galleryRepository.findById(id);
    }

    @GetMapping("/galleries")
    @PreAuthorize("permitAll()")
    public List<Gallery> getGalleries() {
        return galleryRepository.findAll();
    }

    @PostMapping("/gallery")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<Object> putGallery(@RequestPart(value="header") String header,
                                             @RequestPart(value="file") final MultipartFile file) {
            Gallery gallery = new Gallery();
            gallery.setHeader(header);
            gallery.setPhotoPath("/fake/path");
            gallery = galleryRepository.save(gallery);
            gallery.setPhotoPath(uploadFileService.uploadPhoto(gallery.getId(), file, PicEntities.Galleries));
            galleryRepository.save(gallery);

            return new ResponseEntity<>(
                    "GALLERY CREATED",
                    HttpStatus.OK);
    }


    @DeleteMapping("/gallery/{id}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public String deleteGallery(@PathVariable("id") long id) {
        if (galleryRepository.existsById(id)) {
            galleryRepository.deleteById(id);
            return "GALLERY DELETED";
        }
        return "GALLERY EVENT ID";
    }

    @GetMapping("/galleries/{id:[\\d]+}/{img_name}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Resource> getGalleryImage(@PathVariable("id") long id,
                                                  @PathVariable("img_name") String imageName) {
        Path path = Paths.get(String.format(uploadFileService.getPathFormat(PicEntities.Galleries),id, imageName));

        HttpHeaders header = new HttpHeaders();
        header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + path.getFileName());
        header.add("Cache-Control", "no-cache, no-store, must-revalidate");
        header.add("Pragma", "no-cache");
        header.add("Expires", "0");

        FileSystemResource resource = new FileSystemResource(path);
        return ResponseEntity.ok()
                .headers(header)
                .contentLength(path.toFile().length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    /********POLLS********/
    @GetMapping("/poll/{id:\\d+}")
    @PreAuthorize("permitAll()")
    public Optional<Poll> getPoll(@PathVariable("id") long id) {
        return pollRepository.findById(id);
    }

    @GetMapping("/polls")
    @PreAuthorize("permitAll()")
    public List<Poll> getPolls() {
        return pollRepository.findAll();
    }

    @PostMapping("/poll")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<Object> putPoll(@RequestBody Map<String, Object> payload) {
        Poll poll = new Poll();
        poll.setTitle((String) payload.get("title"));
        poll.setDate((String) payload.get("date"));
        poll = pollRepository.save(poll);

        List<String> titles = (List<String>) payload.get("options");
        for ( String title : titles) {
            Option option = new Option();
            option.setTitle(title);
            option.setPoll(poll);
            optionRepository.save(option);
        }

        return new ResponseEntity<>(
                "POLL CREATED",
                HttpStatus.OK);
    }

    @DeleteMapping("/poll/{id}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public String deletePoll(@PathVariable("id") long id) {
        if (pollRepository.existsById(id)) {
            pollRepository.deleteById(id);
            return "POLL DELETED";
        }
        return "POLL EVENT ID";
    }

    /********OPTIONS********/
    @GetMapping("/poll/{pid:\\d+}/options")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public List<Option> getPollOptions(@PathVariable("pid") long pid) {
        List<Option> options = new ArrayList<>();
        Optional<Poll> poll = pollRepository.findById(pid);
        poll.ifPresent((p)-> options.addAll( p.getOptions()));
        return options;
    }

    @GetMapping("/option/{id:\\d+}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public Optional<Option> getOption(@PathVariable("id") long id) {
        return optionRepository.findById(id);
    }

    @GetMapping("/options")
    @PreAuthorize("permitAll()")
    public List<Option> getOptions() {
        return optionRepository.findAll();
    }

    @PostMapping("/option")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<Object> putOption(@RequestBody Map<String, Object> payload) {
        Option option = new Option();
        option.setId((long)(Integer) payload.get("id"));
        option.setTitle((String) payload.get("title"));
        option.setPoll(pollRepository.findById(
               (long)((Integer) payload.get("poll")))
               .orElseThrow(InvalidParameterException::new));
        optionRepository.save(option);

        return new ResponseEntity<>(
                "OPTION UPDATED",
                HttpStatus.OK);
    }

    @PostMapping("/vote")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<Object> putVote(@RequestBody Map<String, Object> payload) {

        Optional<Option> optionalOption = optionRepository.findById((long)(Integer) payload.get("id"));
        Option option = optionalOption.orElseThrow(InvalidParameterException::new);
        option.setVotes(option.getVotes() + 1);
        optionRepository.save(option);

        return new ResponseEntity<>(
                "OPTION VOTED",
                HttpStatus.OK);
    }


    @DeleteMapping("/option/{id}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public String deleteOption(@PathVariable("id") long id) {
        if (optionRepository.existsById(id)) {
            optionRepository.deleteById(id);
            return "OPTION DELETED";
        }
        return "OPTION COMMENT ID";
    }

    /*STUFFS*/
    @GetMapping("/stuffs/{img_name}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Resource> getStuffImage(@PathVariable("img_name") String imageName) {
        Path path = Paths.get(uploadFileService.getRootFolder() + "Stuffs/" + imageName);

        HttpHeaders header = new HttpHeaders();
        header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + path.getFileName());
        header.add("Cache-Control", "no-cache, no-store, must-revalidate");
        header.add("Pragma", "no-cache");
        header.add("Expires", "0");

        FileSystemResource resource = new FileSystemResource(path);
        return ResponseEntity.ok()
                .headers(header)
                .contentLength(path.toFile().length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    private Set<Authority> boundAuthorities(String authorities) {
        authorities = authorities
                        .replaceAll("\\s", "")
                        .toUpperCase();
        String[] auths = authorities.split(",");
        return Arrays.stream(auths).map(aname -> {
            Authority authority = authorityRepo.findByAname("ROLE_" + aname)
                    .orElseThrow(InvalidParameterException::new);
            return authority;
        }).collect(Collectors.toSet());

    }

}
