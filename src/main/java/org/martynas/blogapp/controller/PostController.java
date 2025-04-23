package org.martynas.blogapp.controller;

import java.security.Principal;
import java.util.Optional;

import javax.validation.Valid;

import org.martynas.blogapp.model.BlogUser;
import org.martynas.blogapp.model.Post;
import org.martynas.blogapp.service.BlogUserService;
import org.martynas.blogapp.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

@Controller
@SessionAttributes("post")
public class PostController {

    private final PostService postService;
    private final BlogUserService blogUserService;

    @Autowired
    public PostController(PostService postService, BlogUserService blogUserService) {
        this.postService = postService;
        this.blogUserService = blogUserService;
    }

    private String getAuthenticatedUsername(Principal principal) {
        return (principal != null) ? principal.getName() : "anonymousUser";
    }

    private boolean isUserOwnerOfPost(Post post, String username) {
        return post.getUser().getUsername().equals(username);
    }

    @GetMapping("/post/{id}")
    public String getPost(@PathVariable Long id, Model model, Principal principal) {
        String authUsername = getAuthenticatedUsername(principal);

        Optional<Post> optionalPost = this.postService.getById(id);
        if (optionalPost.isPresent()) {
            Post post = optionalPost.get();
            model.addAttribute("post", post);
            if (isUserOwnerOfPost(post, authUsername)) {
                model.addAttribute("isOwner", true);
            }
            return "post";
        } else {
            return "404";
        }
    }

    @Secured("ROLE_USER")
    @GetMapping("/createNewPost")
    public String createNewPostForm(Model model, Principal principal) {
        String authUsername = getAuthenticatedUsername(principal);
        Optional<BlogUser> optionalBlogUser = this.blogUserService.findByUsername(authUsername);
        if (optionalBlogUser.isPresent()) {
            Post post = new Post();
            post.setUser(optionalBlogUser.get());
            model.addAttribute("post", post);
            return "postForm";
        } else {
            return "error";
        }
    }

    @Secured("ROLE_USER")
    @PostMapping("/createNewPost")
    public String createNewPost(@Valid @ModelAttribute Post post, BindingResult bindingResult, SessionStatus sessionStatus) {
        if (bindingResult.hasErrors()) {
            return "postForm";
        }
        this.postService.save(post);
        sessionStatus.setComplete();
        return "redirect:/post/" + post.getId();
    }

    @Secured("ROLE_USER")
    @GetMapping("editPost/{id}")
    public String editPost(@PathVariable Long id, Model model, Principal principal) {
        String authUsername = getAuthenticatedUsername(principal);

        Optional<Post> optionalPost = this.postService.getById(id);
        if (optionalPost.isPresent()) {
            Post post = optionalPost.get();
            if (isUserOwnerOfPost(post, authUsername)) {
                model.addAttribute("post", post);
                return "postForm";
            } else {
                return "403";
            }
        } else {
            return "error";
        }
    }

    @Secured("ROLE_USER")
    @GetMapping("/deletePost/{id}")
    public String deletePost(@PathVariable Long id, Principal principal) {
        String authUsername = getAuthenticatedUsername(principal);

        Optional<Post> optionalPost = this.postService.getById(id);
        if (optionalPost.isPresent()) {
            Post post = optionalPost.get();
            if (isUserOwnerOfPost(post, authUsername)) {
                this.postService.delete(post);
                return "redirect:/";
            } else {
                return "403";
            }
        } else {
            return "error";
        }
    }
}