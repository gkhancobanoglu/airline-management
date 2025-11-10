"use client";
import { AppBar, Toolbar, Typography, Button, Box } from "@mui/material";
import Link from "next/link";
import { useRouter, usePathname } from "next/navigation";
import { useEffect, useState } from "react";
import { getUserRole, isAuthenticated, logout } from "@/services/authService";

export default function Navbar() {
  const router = useRouter();
  const pathname = usePathname();
  const [role, setRole] = useState<string | null>(null);
  const [authed, setAuthed] = useState<boolean | null>(null);

  useEffect(() => {
    const loggedIn = isAuthenticated();

    Promise.resolve().then(() => {
      if (!loggedIn) {
        logout();
        setAuthed(false);
        setRole(null);
        return;
      }
      setAuthed(true);
      setRole(getUserRole());
    });
  }, [pathname]);

  const handleLogout = () => {
    logout();
    setAuthed(false);
    setRole(null);
    router.push("/login");
  };

  const isAdmin = role === "ADMIN";
  const isUser = role === "USER";

  if (authed === null) return null;

  return (
    <AppBar position="fixed" sx={{ background: "#0D47A1" }}>
      <Toolbar sx={{ justifyContent: "space-between" }}>
        <Typography
          variant="h6"
          sx={{ fontWeight: 600, letterSpacing: 0.5, cursor: "pointer" }}
          onClick={() => router.push("/")}
        >
          Airline Management
        </Typography>

        <Box>
          {!authed ? (
            <>
              <Button color="inherit" component={Link} href="/login">
                LOGIN
              </Button>
              <Button color="inherit" component={Link} href="/register">
                REGISTER
              </Button>
            </>
          ) : (
            <>
              <Button color="inherit" component={Link} href="/">
                DASHBOARD
              </Button>

              {isAdmin && (
                <>
                  <Button color="inherit" component={Link} href="/airlines">
                    AIRLINES
                  </Button>
                  <Button color="inherit" component={Link} href="/passengers">
                    PASSENGERS
                  </Button>
                </>
              )}

              <Button color="inherit" component={Link} href="/flights">
                FLIGHTS
              </Button>
              <Button color="inherit" component={Link} href="/bookings">
                BOOKINGS
              </Button>

              <Button
                color="inherit"
                disabled
                sx={{
                  ml: 2,
                  fontWeight: 600,
                  background: "#1565C0",
                  borderRadius: "6px",
                  px: 1.5,
                  "&:hover": { background: "#1976D2" },
                }}
              >
                {isAdmin ? "🛡️ ADMIN" : isUser ? "👤 USER" : ""}
              </Button>

              <Button
                color="inherit"
                onClick={handleLogout}
                sx={{
                  ml: 1,
                  fontWeight: 600,
                  "&:hover": { color: "#FFCDD2" },
                }}
              >
                LOGOUT
              </Button>
            </>
          )}
        </Box>
      </Toolbar>
    </AppBar>
  );
}
