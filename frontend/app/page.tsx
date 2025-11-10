"use client";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { Box, Paper, Typography } from "@mui/material";
import Link from "next/link";
import { getUserRole, isAuthenticated } from "@/services/authService";

export default function Dashboard() {
  const router = useRouter();
  const [role, setRole] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!isAuthenticated()) {
      router.push("/login");
      return;
    }

    const timer = setTimeout(() => {
      const userRole = getUserRole();
      setRole(userRole);
      setLoading(false);
    }, 0);

    return () => clearTimeout(timer);
  }, [router]);

  if (loading) {
    return (
      <Box
        display="flex"
        justifyContent="center"
        alignItems="center"
        height="80vh"
      >
        <Typography variant="h6" color="text.secondary">
          Loading Dashboard...
        </Typography>
      </Box>
    );
  }

  const allCards = [
    {
      title: "AIRLINES",
      gradient: "linear-gradient(135deg, #2196F3 0%, #21CBF3 100%)",
      path: "/airlines",
      roles: ["ADMIN"],
    },
    {
      title: "FLIGHTS",
      gradient: "linear-gradient(135deg, #43A047 0%, #66BB6A 100%)",
      path: "/flights",
      roles: ["ADMIN", "USER"],
    },
    {
      title: "PASSENGERS",
      gradient: "linear-gradient(135deg, #FB8C00 0%, #FFB300 100%)",
      path: "/passengers",
      roles: ["ADMIN"],
    },
    {
      title: "BOOKINGS",
      gradient: "linear-gradient(135deg, #8E24AA 0%, #BA68C8 100%)",
      path: "/bookings",
      roles: ["ADMIN", "USER"],
    },
  ];

  const visibleCards = allCards.filter(
    (card) => role && card.roles.includes(role)
  );

  return (
    <Box sx={{ mt: 4 }}>
      <Typography
        variant="h3"
        gutterBottom
        sx={{
          fontWeight: 700,
          mb: 6,
          color: "#0D47A1",
          letterSpacing: "-0.5px",
          textAlign: "center",
        }}
      >
        Airline Management Dashboard
      </Typography>

      {/* Grid yerine Box tabanlı düzen */}
      <Box
        display="flex"
        flexWrap="wrap"
        justifyContent="center"
        alignItems="stretch"
        sx={{ maxWidth: "1200px", margin: "0 auto", gap: 4 }}
      >
        {visibleCards.map((card) => (
          <Box
            key={card.title}
            flexBasis={{ xs: "100%", sm: "45%", md: "22%" }}
            display="flex"
            justifyContent="center"
          >
            <Link
              href={card.path}
              style={{
                textDecoration: "none",
                color: "inherit",
                width: "100%",
                display: "flex",
                justifyContent: "center",
              }}
            >
              <Paper
                elevation={8}
                sx={{
                  p: 5,
                  textAlign: "center",
                  background: card.gradient,
                  color: "white",
                  borderRadius: 4,
                  transition: "all 0.35s ease",
                  cursor: "pointer",
                  height: "180px",
                  width: "100%",
                  maxWidth: "250px",
                  display: "flex",
                  flexDirection: "column",
                  justifyContent: "center",
                  alignItems: "center",
                  "&:hover": {
                    transform: "translateY(-5px)",
                    boxShadow: "0 10px 25px rgba(0,0,0,0.25)",
                  },
                }}
              >
                <Typography variant="h5" fontWeight="bold" sx={{ mb: 1 }}>
                  {card.title}
                </Typography>
                <Typography
                  variant="h3"
                  fontWeight="bold"
                  sx={{
                    opacity: 0.9,
                    fontSize: "2.8rem",
                  }}
                >
                  --
                </Typography>
              </Paper>
            </Link>
          </Box>
        ))}
      </Box>

      {visibleCards.length === 0 && (
        <Typography
          align="center"
          color="text.secondary"
          sx={{ mt: 4, fontSize: 18 }}
        >
          No sections available for your role.
        </Typography>
      )}
    </Box>
  );
}
