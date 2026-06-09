import { createTheme } from '@mui/material/styles';

export const darkTheme = createTheme({
  palette: {
    mode: 'dark',
    primary: {
      main: '#2563EB', // Blue 600
    },
    secondary: {
      main: '#1E293B', // Slate 800
    },
    error: {
      main: '#EF4444', // Live Red
    },
    success: {
      main: '#22C55E', // Success Green
    },
    warning: {
      main: '#F59E0B', // Warning Orange
    },
    background: {
      default: '#020617', // Slate 950
      paper: '#111827', // Slate 900
    },
    text: {
      primary: '#FFFFFF',
      secondary: '#94A3B8',
    }
  },
  typography: {
    fontFamily: '"Inter", "Roboto", "Helvetica", "Arial", sans-serif',
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 8,
          textTransform: 'none',
          fontWeight: 600,
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          borderRadius: 16,
          backgroundImage: 'none',
          boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)',
          border: '1px solid #1E293B',
        },
      },
    },
    MuiDrawer: {
      styleOverrides: {
        paper: {
          backgroundColor: '#020617',
          borderRight: '1px solid #1E293B',
        }
      }
    },
    MuiAppBar: {
      styleOverrides: {
        root: {
          backgroundColor: '#020617',
          backgroundImage: 'none',
          borderBottom: '1px solid #1E293B',
          boxShadow: 'none',
        }
      }
    }
  },
});
