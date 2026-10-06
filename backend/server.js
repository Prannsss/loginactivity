const express = require("express");

const app = express();

const PORT = process.env.PORT || 3000;

// Allow Express to read JSON request bodies
app.use(express.json());

/*
 * Dummy login credentials
 *
 * These are ONLY for the school project.
 * Do not store real passwords like this in a production application.
 */
const DUMMY_USERNAME = "student";
const DUMMY_PASSWORD = "password123";

// Health check
app.get("/", (req, res) => {
    res.json({
        success: true,
        message: "Login Activity API is running"
    });
});

// Login endpoint
app.post("/api/login", (req, res) => {

    const { username, password } = req.body;

    // Validate that both fields were provided
    if (!username || !password) {
        return res.status(400).json({
            success: false,
            message: "Username and password are required"
        });
    }

    // Validate credentials
    if (
        username === DUMMY_USERNAME &&
        password === DUMMY_PASSWORD
    ) {
        return res.status(200).json({
            success: true,
            message: "Login successful"
        });
    }

    // Invalid credentials
    return res.status(401).json({
        success: false,
        message: "Invalid username or password"
    });
});

// Render requires the server to listen on the provided PORT
app.listen(PORT, "0.0.0.0", () => {
    console.log(`Server running on port ${PORT}`);
});