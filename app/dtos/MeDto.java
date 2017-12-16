package dtos;

import java.util.Objects;

/**
 * Created by Selim Eren Bekçe on 16.08.2017.
 */
public class MeDto {
    private String id;
    private String name;
    private String email;

    public MeDto() {
    }

    public MeDto(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MeDto meDto = (MeDto) o;
        return Objects.equals(id, meDto.id) &&
                Objects.equals(name, meDto.name) &&
                Objects.equals(email, meDto.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, email);
    }

    @Override
    public String toString() {
        return "MeDto{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
