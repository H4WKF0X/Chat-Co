package com.chatco.chatco.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "room")
/**
 * Physical room that can be assigned to meetings.
 */
public class Room {
    /** Human readable location, for example building and floor. */
    @Size(max = 255)
    @NotNull
    @Column(name = "location", nullable = false)
    private String location;

    /** Maximum number of people the room can hold. */
    @NotNull
    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    /** Unique or display name used to find the room. */
    @Size(max = 150)
    @NotNull
    @Column(name = "name", nullable = false, length = 150)
    private String name;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

}
