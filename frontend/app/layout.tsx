"use client";

import { ReactNode, Suspense } from "react";
import { CssBaseline, Container } from "@mui/material";
import dynamic from "next/dynamic";

const Navbar = dynamic(() => import("../components/Navbar"), { ssr: false });

export default function RootLayout({ children }: { children: ReactNode }) {
  return (
    <html lang="tr">
      <body>
        <CssBaseline />

        <Suspense fallback={null}>
          <Navbar />
        </Suspense>

        <Container maxWidth="lg" sx={{ mt: 10, mb: 4 }}>
          {children}
        </Container>
      </body>
    </html>
  );
}
