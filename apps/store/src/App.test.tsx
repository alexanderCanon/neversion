import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import { Offers } from './pages/Offers'
import { MemoryRouter } from 'react-router-dom'

describe('Offers Component', () => {
  it('renders heading and placeholder message', () => {
    render(
      <MemoryRouter>
        <Offers />
      </MemoryRouter>
    )

    expect(screen.getByText(/OFERTAS Y/i)).toBeInTheDocument()
    expect(screen.getByText(/Estamos preparando ofertas exclusivas/i)).toBeInTheDocument()
    expect(screen.getByText(/Ver Catálogo Activo/i)).toBeInTheDocument()
  })
})
