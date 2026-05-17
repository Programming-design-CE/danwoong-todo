package com.danwoog.todo.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String note;
    
    public Member(String name) { 
        this.name = name; 
    }
    
    public void updateNote(String note) { 
        this.note = note; 
    }
}
