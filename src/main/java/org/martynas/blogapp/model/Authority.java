package org.martynas.blogapp.model;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;
import java.util.Collection;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "users")
@Entity
@Table(name = "authorities")
@SequenceGenerator(name = "authority_seq_gen", sequenceName = "authority_seq", initialValue = 10, allocationSize = 1)
public class Authority implements GrantedAuthority {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "authority_seq_gen")
    @Column(name = "id")
    private Long id;

    @Column(name = "authority", unique = true, nullable = false)
    private String authority;

    @ManyToMany(mappedBy = "authorities", cascade = CascadeType.ALL)
    private Collection<BlogUser> users;

    @Override
    public String getAuthority() {
        return this.authority;
    }
}
