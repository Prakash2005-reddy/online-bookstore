package com.chapterco.bookstore.config;

import com.chapterco.bookstore.model.AppUser;
import com.chapterco.bookstore.model.Book;
import com.chapterco.bookstore.model.Category;
import com.chapterco.bookstore.repository.BookRepository;
import com.chapterco.bookstore.repository.CategoryRepository;
import com.chapterco.bookstore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Component
public class SeedData implements CommandLineRunner {
    private final CategoryRepository categories;
    private final BookRepository books;
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_PASSWORD:admin12345}")
    private String adminPassword;

    public SeedData(CategoryRepository categories, BookRepository books, UserRepository users,
                    PasswordEncoder passwordEncoder) {
        this.categories = categories;
        this.books = books;
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        createAdmin();
        if (books.count() > 0) return;

        Category fiction = category("Fiction", "fiction");
        Category nonfiction = category("Non-fiction", "non-fiction");
        Category fantasy = category("Fantasy", "fantasy");
        Category mystery = category("Mystery", "mystery");
        Category wellbeing = category("Wellbeing", "wellbeing");
        List<Book> samples = List.of(
                new Book("The Midnight Library", "Matt Haig", "Between life and death there is a library, and within that library, the shelves go on forever. A thoughtful novel about second chances and the lives we might have lived.", "9780525559474", "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&w=800&q=85", new BigDecimal("18.99"), new BigDecimal("4.8"), 24, fiction),
                new Book("The Quiet Atlas", "Mara Ellison", "A cartographer returns to her coastal hometown and discovers that the maps she draws are changing the memories of the people who live there.", "9781639100021", "https://images.unsplash.com/photo-1512820790803-83ca734da794?auto=format&fit=crop&w=800&q=85", new BigDecimal("16.50"), new BigDecimal("4.6"), 18, fiction),
                new Book("A Field Guide to Wonder", "Elliot Finch", "Small observations, gentle invitations, and practical ways to pay closer attention to the ordinary world around us.", "9780593321201", "https://images.unsplash.com/photo-1519682337058-a94d519337bc?auto=format&fit=crop&w=800&q=85", new BigDecimal("21.00"), new BigDecimal("4.9"), 31, nonfiction),
                new Book("The Lantern Keeper", "S. Rowan Bell", "On a far northern island, a new lighthouse keeper finds a journal that records storms before they happen—and a secret the village has kept for generations.", "9780063354028", "https://images.unsplash.com/photo-1513001900722-370f803f498d?auto=format&fit=crop&w=800&q=85", new BigDecimal("17.25"), new BigDecimal("4.7"), 12, mystery),
                new Book("Small Hours, Big Skies", "Nadia Sol", "An imaginative journey through a world of floating gardens, old stargazers, and the unlikely friendships that keep a city aloft.", "9781250881646", "https://images.unsplash.com/photo-1519791883288-dc8bd696e667?auto=format&fit=crop&w=800&q=85", new BigDecimal("19.75"), new BigDecimal("4.5"), 15, fantasy),
                new Book("The Art of a Slower Day", "Clara Bennett", "A warm, practical companion for finding more room to breathe, make things, and enjoy the rhythms of everyday life.", "9780593541297", "https://images.unsplash.com/photo-1516979187457-637abb4f9353?auto=format&fit=crop&w=800&q=85", new BigDecimal("22.00"), new BigDecimal("4.8"), 27, wellbeing),
                new Book("Postcards from Elsewhere", "Jonah Reed", "A collection of linked stories told through letters and postcards, following strangers whose lives cross in a little bookshop.", "9780063318243", "https://images.unsplash.com/photo-1514894780887-121968d00567?auto=format&fit=crop&w=800&q=85", new BigDecimal("15.99"), new BigDecimal("4.4"), 9, fiction),
                new Book("The Last Good Orchard", "Bea Marlowe", "When an inherited orchard is threatened, a brother and sister uncover a decades-old mystery rooted beneath its oldest tree.", "9780593657982", "https://images.unsplash.com/photo-1507842217343-583bb7270b66?auto=format&fit=crop&w=800&q=85", new BigDecimal("18.00"), new BigDecimal("4.7"), 20, mystery)
        );
        books.saveAll(samples);
    }

    private void createAdmin() {
        if (!users.existsByEmailIgnoreCase("admin@chapterco.com")) {
            users.save(new AppUser("Chapter & Co. Admin", "admin@chapterco.com",
                    passwordEncoder.encode(adminPassword), "ADMIN"));
        }
    }

    private Category category(String name, String slug) {
        return categories.findBySlug(slug).orElseGet(() -> categories.save(new Category(name, slug)));
    }
}
